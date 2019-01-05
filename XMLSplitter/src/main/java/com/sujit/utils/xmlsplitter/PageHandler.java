package com.sujit.utils.xmlsplitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

public class PageHandler extends DefaultHandler {
	
	private static Logger LOGGER = Logger.getLogger(PageHandler.class.getName());
	
	private StringBuffer chunkString;
	private int pageCount;
	private int maxTags;
	private int fileRollerIndex;
	private String chunkFileSuffix;
	private String splitTagName;
	private String rootTagName;
	private String outPutFolder;
	
	
	public PageHandler(int maxTagLimit, String splitTagName, String rootTagName, String fileSuffix, String outputFolder){
		super();
		chunkString = new StringBuffer();
		this.maxTags = maxTagLimit;
		this.splitTagName = splitTagName;
		this.rootTagName = rootTagName;
		this.chunkFileSuffix = fileSuffix;
		this.outPutFolder = outputFolder;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if(!qName.equals(rootTagName)){
			if(attributes.getLength()>0){
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<attributes.getLength(); i++) {
					sb.append(" ").append(attributes.getQName(i)).append("=\"").append(getXMLEscapedString(attributes.getValue(i))).append("\"");
				}
				chunkString.append("<" + qName + sb.toString() +">");
			}else{
				chunkString.append("<" + qName + ">");
			}
		}
	}
	
	@Override
	  public void endElement(String uri, String localName, String qName) {
		chunkString.append("</" + qName + ">");
	        if (qName.equals(splitTagName)) {
	            pageCount++;
	        }
	       if (pageCount < maxTags) {
	    	   if(qName.equals(rootTagName)){
	    		   //rollup the last set of tags
	    		   rollFile(prettyFormat("<"+ rootTagName +">"+chunkString.toString()));
	    		   //rollFile("<"+ rootTagName +">"+showTimeText.toString());
	    	   }else{
	            // do nothing
	    	   }
	        }else{
	        	//rollFile("<"+ rootTagName +">"+showTimeText.toString()+"</"+ rootTagName +">");
	        	rollFile(prettyFormat("<"+ rootTagName +">"+chunkString.toString()+"</"+ rootTagName +">"));
	        }
	    }
	
	@Override
	  public void characters(char[] chars, int start, int length) {
	        for (int i = start; i < start + length; i++) {
	        	if(chars[i]=='&'){
	        		chunkString.append("&amp;");
	        	}else if(chars[i]=='"'){
	        		chunkString.append("&quot;");
	        	}else if(chars[i]=='\''){
	        		chunkString.append("&apos;");
	        	}else if(chars[i]=='<'){
	        		chunkString.append("&lt;");
	        	}else if(chars[i]=='>'){
	        		chunkString.append("&gt;");
	        	}else{
	        		chunkString.append(chars[i]);
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
		//System.out.println(input);
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // <--- check point 
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
		File optFile = new File(outPutFolder+File.separator+chunkFileSuffix+fileRollerIndex+".xml");
		//FileWriter fileWriter = null;
		CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(optFile),encoder));
			/*
			 * Taking off the use of the FileWriter as it cannot write the files in UTF encoding format, and instead, by default 
			 * writes them in the system encoding, which is ANSI for Windows :-(
			 */
			/*fileWriter = new FileWriter(optFile);
			fileWriter.write(content);
            fileWriter.close();*/
			out.write(content);
			out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
            try {
                //fileWriter.close();
            	if (out != null) {
            		out.flush();
            		out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            LOGGER.info("Writing complete for file : "+optFile.getName());
            chunkString = new StringBuffer();
            fileRollerIndex ++;
            pageCount = 0;
        }
	}
}
