package com.sujit.utils.xmlextractor;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import java.util.logging.Logger;


/**
 * 
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
    	String targetTagName = "";
    	String rootTagName = "";
    	String targetTagCountLimit = "";
    	//String chunkFileNameSuffix = "";
    	if(args.length<5){
    		LOGGER.severe("Not enough input parameters. Please check POM for all the required input parameters.");
    		return;
    	}else{
    		LOGGER.info("Source File Path :"+args[0]);
    		LOGGER.info("Output Folder Path :"+args[1]);
    		LOGGER.info("Target Tag Name :"+args[2]);
    		LOGGER.info("Root Tag Name :"+args[3]);
    		LOGGER.info("Tag count limit :"+args[4]);
    		sourceFilePath = args[0];
    		outputFolderPath = args[1];
    		targetTagName = args[2];
    		rootTagName = args[3];
    		targetTagCountLimit = args[4];
    	}
    	int maxLimit = Integer.parseInt(targetTagCountLimit);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			PageHandler ph = new PageHandler(maxLimit,targetTagName,rootTagName,outputFolderPath);
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
