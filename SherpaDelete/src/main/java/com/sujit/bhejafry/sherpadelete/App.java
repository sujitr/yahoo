package com.sujit.bhejafry.sherpadelete;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URLEncoder;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.sujit.utility.Zipper;
import com.sujit.yahoo.mailer.SendMail.MailerApp;

import java.util.logging.Logger;

public class App {
	private final static Logger LOGGER = Logger.getLogger(App.class .getName());
	@SuppressWarnings("deprecation")
	public static void main( String[] args ) {
    	String hostNamePart = "";
    	String headerValue = "";
    	String listFilePath = "";
    	if(args.length<3){
    		LOGGER.severe("Not enough input parameters. Need the query URL string,'Yahoo-App-Auth' header value and source file location (in that order).");
    		return;
    	}else{
    		LOGGER.info("Obtained Query URL :"+args[0]);
    		LOGGER.info("Obtained Header Info :"+args[1]);
    		LOGGER.info("Obtained Source File Path :"+args[2]);
    		hostNamePart = args[0];
    		headerValue = args[1];
    		listFilePath = args[2];
    	}
    	Scanner scanner = null;
    	BufferedWriter bufferedWriter = null;
    	String key = null;
    	String uuidValue = null;
    	String resFilePath1 = null;
    	String zipFilePath = null;
        try {
         scanner = new Scanner(new FileReader(listFilePath));
         File listFile = new File(listFilePath);
         String outputFilePath = listFile.getPath();
     	 outputFilePath = outputFilePath.substring(0, outputFilePath.lastIndexOf(File.separator));
         bufferedWriter = new BufferedWriter(new FileWriter(outputFilePath+File.separator+"SherpaDeleteFailureLog.txt",true));
         resFilePath1 = outputFilePath+File.separator+"SherpaDeleteFailureLog.txt";
         zipFilePath = outputFilePath+File.separator+"Results.zip";
         Calendar startTime = Calendar.getInstance();
         while ( scanner.hasNextLine() ){
        	  String value = scanner.nextLine();
        	  LOGGER.info("Reading a line from file as : "+value);
        	  String[] x = value.split("\\|");
        	  key = x[0];
        	  uuidValue = x[1];
        	  String deleteUrl = hostNamePart+URLEncoder.encode(key);
        	  LOGGER.info("Delete URL - "+deleteUrl);
        	  int deleteStatus = deleteSherpaEntry(deleteUrl, headerValue);
        	  if(deleteStatus==200){
        		  // just chill
        	  }else{
        		  //damn, write the entry in a log file for a retry later
        		  bufferedWriter.write(key+"|"+uuidValue);
        		  bufferedWriter.newLine();
        		  bufferedWriter.flush();
        	  }
        	  Calendar flagTime = Calendar.getInstance();
         	  int status = intervalNotify(startTime, flagTime, "es");
         	  if(status == 1){
         		 startTime = Calendar.getInstance();
         	  }
          }
        } catch (FileNotFoundException e) {
			LOGGER.severe("File Not Found - "+listFilePath);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IOException while deleting the stuffs. Writing this instance in log file.");
			try {
				bufferedWriter.write(key+"|"+uuidValue);
				bufferedWriter.newLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally{
			 scanner.close();
	          try {
	              if (bufferedWriter != null) {
	                  bufferedWriter.flush();
	                  bufferedWriter.close();
	              }
	          } catch (IOException ex) {
	        	  LOGGER.severe("Exception while closing the failure log file.");
	              ex.printStackTrace();
	          }
		}
		File errorFile = new File(resFilePath1);
		if(errorFile.length()==0){
			List<String> inputFiletoZip = new ArrayList<String>();
			inputFiletoZip.add(listFilePath);
			Zipper.makeZip(inputFiletoZip, zipFilePath);
			List<String> fileAttList = new ArrayList<String>();
			fileAttList.add(zipFilePath);
			sendMail("Sherpa Delete Script has finished execution. No errors have been reported. All entries in the attached input file has been deleted", "JAKE alt-src key removal from BK1 table", "skhichi@yahoo-inc.com,smeghana@yahoo-inc.com,smanpr@yahoo-inc.com,sakshat@yahoo-inc.com", fileAttList);
		}else{
			List<String> inputFiletoZip = new ArrayList<String>();
			inputFiletoZip.add(resFilePath1);
			inputFiletoZip.add(listFilePath);
			Zipper.makeZip(inputFiletoZip, zipFilePath);
			List<String> fileAttList = new ArrayList<String>();
			fileAttList.add(zipFilePath);
			sendMail("Sherpa Delete Script has finished execution. However some errors have been encountered while deleteing entries. Id's which failed to be deleted are available at 'SherpaDeleteFailureLog.txt'", "JAKE alt-src key removal from BK1 table", "skhichi@yahoo-inc.com,smeghana@yahoo-inc.com,smanpr@yahoo-inc.com,sakshat@yahoo-inc.com", fileAttList);
		}
    }
    
    
    public static int deleteSherpaEntry(String sherpaURL, String headerValue){
    	int responseData = 0;
    	HttpClient client = new HttpClient();
        client.getHostConfiguration().setProxy("yca-proxy.corp.yahoo.com", 3128);
    	HttpMethod method = new GetMethod(sherpaURL);
    	Header yHeader = new Header("Yahoo-App-Auth",headerValue);
    	method.addRequestHeader(yHeader);
    	try{
    		int statusCode = client.executeMethod(method);
    		byte[] responseBody = method.getResponseBody();
   		 	LOGGER.info("HTTP Status = "+ statusCode +", & HTTP Response for this delete operation is = "+new String(responseBody));
    		if (statusCode != HttpStatus.SC_OK) {
    	        LOGGER.info("Sherpa delete operation failed with message -  " + method.getStatusLine());
    	    }else{
    	    	 LOGGER.info("Sherpa delete operation succeeded.");
    	    }
    		responseData = statusCode;
    	}
    	catch(HttpException e){
    		LOGGER.severe("Fatal protocol violation: " + e.getMessage());
    	    e.printStackTrace();
    	}catch(IOException e){
    		LOGGER.severe("Fatal transport error: " + e.getMessage());
    	    e.printStackTrace();
    	}finally{
    		method.releaseConnection();
    	}
    	return responseData;
    }
    
    public static void sendMail(String bodyPart, String sub, String cclist, List<String> fileAttList){
		try
	    {
	      String to="sujitroy@yahoo-inc.com";
	      String cc = cclist;
	      String from="sujitroy-autogenerate@yahoo-inc.com";
	      String subject=sub;
	      String body=bodyPart+"\n\nDO NOT REPLY TO THIS MESSAGE.";
	      MailerApp ma = new MailerApp();
		  ma.sendMail(to, cc, from, subject, body, fileAttList);
	    }
	    catch (Exception ex)
	    {
	      ex.printStackTrace();
	    }
	}
    
    public static int intervalNotify(Calendar startTime, Calendar flagTime, String regionHeader){
		int status = 0;
		long start = startTime.getTimeInMillis();
		long flag = flagTime.getTimeInMillis();
		long diff = Math.abs(flag - start);
		long diffInSeconds = diff/1000;
		if(diffInSeconds>=900){
			sendMail("JAKE alt-src key deletion script is still executing for "+regionHeader.toUpperCase()+" region", "JAKE alt-src key deletion script is still executing for "+regionHeader.toUpperCase()+" region", "skhichi@yahoo-inc.com,smeghana@yahoo-inc.com,smanpr@yahoo-inc.com,sakshat@yahoo-inc.com", null);
			status = 1;
		}
		return status;
	}
}
