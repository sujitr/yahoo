package com.sujit.utils.xmlextractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.logging.Logger;

/**
 * Handler class to check for the specified tag (with attributes containing specific values) in the xml file and pick them up.
 * @author sujitroy
 *
 */
public class PageHandler extends DefaultHandler {
	
	private static Logger LOGGER = Logger.getLogger(PageHandler.class.getName());
	
	private StringBuffer chunkString;
	private int pageCount;
	private int maxTags;
	private int fileRollerIndex;
	private String outputFileSuffix;
	private String targetTagName;
	private String rootTagName;
	private String outPutFolder;
	boolean isReallyATarget;
	boolean isEndOfFile;
	
	
	public PageHandler(int maxTagLimit, String splitTagName, String rootTagName, String outputFolder) {
		super();
		chunkString = new StringBuffer();
		this.maxTags = maxTagLimit;
		this.targetTagName = splitTagName;
		this.rootTagName = rootTagName;
		this.outPutFolder = outputFolder;
		isReallyATarget = false;
		isEndOfFile = false;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if(qName.equals(rootTagName)){
			if(attributes.getLength()>0){
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<attributes.getLength(); i++) {
					sb.append(" ").append(attributes.getQName(i)).append("=\"").append(getXMLEscapedString(attributes.getValue(i))).append("\"");
				}
				chunkString.append("<" + qName + sb.toString() +">");
			}else{
				chunkString.append("<" + qName + ">");
			}
		}else if(qName.equals(targetTagName) && attributes.getLength()>0){
			String tmsID = attributes.getValue("TMSId");																				// WARNING - hard coded values here
			if(tmsID.startsWith("EP")){																													// WARNING - hard coded values here
				isReallyATarget = true;
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<attributes.getLength(); i++) {
					sb.append(" ").append(attributes.getQName(i)).append("=\"").append(getXMLEscapedString(attributes.getValue(i))).append("\"");
				}
				chunkString.append("<" + qName + sb.toString() +">");
			}
		}else if(isReallyATarget){
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<attributes.getLength(); i++) {
				sb.append(" ").append(attributes.getQName(i)).append("=\"").append(getXMLEscapedString(attributes.getValue(i))).append("\"");
			}
			chunkString.append("<" + qName + sb.toString() +">");
		}
	}
	
	@Override
	  public void endElement(String uri, String localName, String qName) throws FetchLimitExceededException{
		
		if(qName.equals(rootTagName)){
			chunkString.append("</" + qName + ">");
			isEndOfFile = true;
		}else if(qName.equals(targetTagName) && isReallyATarget){
			chunkString.append("</" + qName + ">");
			isReallyATarget = false;
			pageCount++;
		}else{
			chunkString.append("</" + qName + ">");
		}
		
		if(pageCount < maxTags){
			if(isEndOfFile){
				rollFile(prettyFormat(chunkString.toString()));
			}else{
				// do nothing
			}
		}else{
			//forcefully end the chunk and write in a file
			if(isEndOfFile){
				rollFile(prettyFormat(chunkString.toString()));
			}else{
				rollFile(prettyFormat(chunkString.toString()+"</"+ rootTagName +">"));
			}
			// throw the exception to stop further parsing
			throw new FetchLimitExceededException("Limit of counting reached. End of the program!");
		}
	 }
	
	@Override
	  public void characters(char[] chars, int start, int length) {
	        if (isReallyATarget) {
				for (int i = start; i < start + length; i++) {
					if (chars[i] == '&') {
						chunkString.append("&amp;");
					} else if (chars[i] == '"') {
						chunkString.append("&quot;");
					} else if (chars[i] == '\'') {
						chunkString.append("&apos;");
					} else if (chars[i] == '<') {
						chunkString.append("&lt;");
					} else if (chars[i] == '>') {
						chunkString.append("&gt;");
					} else {
						chunkString.append(chars[i]);
					}
				}
			}
	    }  
	
	
	/**
	 * Method to pretty format the XML before writing in a file
	 * @param input
	 * @param indent
	 * @return
	 */
	private String prettyFormat(String input, int indent) {
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); 
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	    	LOGGER.info("Exception :"+e.getMessage());
	    	e.printStackTrace();
	        throw new RuntimeException(e); 
	    }
	}

	private String prettyFormat(String input) {
	    return prettyFormat(input, 2);
	}
	
	private String getXMLEscapedString(String s){
		return StringEscapeUtils.escapeXml(s);
	}
	
	private void rollFile(String content){
		File optFile = new File(outPutFolder+File.separator+outputFileSuffix+fileRollerIndex+".xml");
		CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(optFile),encoder));
			out.write(content);
			out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
            try {
            	if (out != null) {
            		out.flush();
            		out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            LOGGER.info("Writing complete for file : "+optFile.getName());
        }
	}
}
