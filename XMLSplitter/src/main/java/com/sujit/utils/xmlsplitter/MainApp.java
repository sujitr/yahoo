package com.sujit.utils.xmlsplitter;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import java.util.logging.Logger;


/**
 * Main entry class for the splitter. This class uses SAX parser to read the XML and then copy the required
 * number of tags into smaller files, adding root tags as specified.
 * @author sujitroy
 *
 */
public class MainApp {
	
	private final static Logger LOGGER = Logger.getLogger(MainApp.class.getName());
	
	/**
	 * Main method for the application.
	 * @param args
	 */
	public static void main(String[] args) {
		String sourceFilePath = "";
    	String outputFolderPath = "";
    	String splitterTagName = "";
    	String rootTagName = "";
    	String splitTagCountLimit = "";
    	String chunkFileNameSuffix = "";
    	if(args.length<6){
    		LOGGER.severe("Not enough input parameters. Please check POM for all the required input parameters.");
    		return;
    	}else{
    		LOGGER.info("Source File Path :"+args[0]);
    		LOGGER.info("Output Folder Path :"+args[1]);
    		LOGGER.info("Splitter Tag Name :"+args[2]);
    		LOGGER.info("Root Tag Name :"+args[3]);
    		LOGGER.info("Chunk count limit :"+args[4]);
    		LOGGER.info("Chunk file name suffix :"+args[5]);
    		sourceFilePath = args[0];
    		outputFolderPath = args[1];
    		splitterTagName = args[2];
    		rootTagName = args[3];
    		splitTagCountLimit = args[4];
    		chunkFileNameSuffix = args[5];
    	}
    	int maxLimit = Integer.parseInt(splitTagCountLimit);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			PageHandler ph = new PageHandler(maxLimit,splitterTagName,rootTagName,chunkFileNameSuffix,outputFolderPath);
			saxParser.parse(sourceFilePath, ph);
		} catch (ParserConfigurationException e) {
			LOGGER.severe("Exception: "+e.getMessage());
		} catch (SAXException e) {
			LOGGER.severe("Exception: "+e.getMessage());
		} catch (IOException e) {
			LOGGER.severe("Exception: "+e.getMessage());
		}
	}

}
