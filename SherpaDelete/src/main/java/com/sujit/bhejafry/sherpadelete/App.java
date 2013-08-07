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

import yjava.security.yca.YCAException;
import com.sujit.test.ycatest.YcaReader;
import com.sujit.utility.zipapp.Zipper;
import com.sujit.yahoo.mailer.SendMail.MailerApp;

import org.apache.log4j.Logger;

/**
 * Class to delete the records from sherpa based on a input file having the key as first column
 * and any value in the second column. The column separator is "|" symbol.
 * The output files from execution of this program will be placed in the same folder of the input file.
 * @author sujitroy
 *
 */
public class App {
	private final static Logger LOGGER = Logger.getLogger(App.class .getName());
	@SuppressWarnings("deprecation")
	public static void main( String[] args ) {
    	String hostNamePart = "";
    	String ycaAppId = "";
    	String listFilePath = "";
    	if(args.length<3){
    		LOGGER.error("Not enough input parameters. Need the query URL string, yahoo yca appid and source file location (in that order).");
    		return;
    	}else{
    		LOGGER.info("Obtained Query URL :"+args[0]);
    		LOGGER.info("Obtained YCA AppID :"+args[1]);
    		LOGGER.info("Obtained Source File Path :"+args[2]);
    		hostNamePart = args[0];
    		ycaAppId = args[1];
    		listFilePath = args[2];
    	}
    	String ycaHeaderValue = "";
    	try {
			ycaHeaderValue = YcaReader.getYcaCertificateValue(ycaAppId);
			LOGGER.info("YCA Header obtained is:"+ycaHeaderValue);
		} catch (YCAException e1) {
			LOGGER.error("Error occurred while getting YCA certificate", e1);
		}
    	
    	if(ycaHeaderValue==null || ycaHeaderValue.isEmpty()){
    		LOGGER.error("Unable to obtain yca app certificate value for the given app-id on this machine. Please check the yca details.");
    		return;
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
         zipFilePath = outputFilePath+File.separator+"DeletionResults.zip";
         Calendar startTime = Calendar.getInstance();
         while ( scanner.hasNextLine() ){
        	  String value = scanner.nextLine();
        	  LOGGER.info("Reading a line from file as : "+value);
        	  String[] x = value.split("\\|");
        	  key = x[0];
        	  uuidValue = x[1];
        	  String deleteUrl = hostNamePart+URLEncoder.encode(key);
        	  LOGGER.info("Delete URL - "+deleteUrl);
        	  int deleteStatus = deleteSherpaEntry(deleteUrl, ycaHeaderValue);
        	  if(deleteStatus==200){
        		  // just chill
        	  }else{
        		  //damn, write the entry in a log file for a retry later
        		  bufferedWriter.write(key+"|"+uuidValue+"|"+deleteStatus +" - HTTP response from Sherpa");
        		  bufferedWriter.newLine();
        		  bufferedWriter.flush();
        	  }
        	  Calendar flagTime = Calendar.getInstance();
         	  int status = intervalNotify(startTime, flagTime);
         	  if(status == 1){
         		 startTime = Calendar.getInstance();
         	  }
          }
        } catch (FileNotFoundException e) {
			LOGGER.error("File Not Found - "+listFilePath);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("IOException while deleting the stuffs. Writing this instance in log file.");
			try {
				bufferedWriter.write(key+"|"+uuidValue+"|"+" - error while deletion: "+e.getMessage());
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
	        	  LOGGER.error("Exception while closing the failure log file.");
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
			sendMail("<h3>Sherpa Delete Script has finished execution on soln-stage</h3><br><p>No errors have been reported. All tribune media alt-src entries in the attached input file has been deleted.<br>Attached zip file contains all the TMS alternate keys which were deleted from the table.</p>", "TMS ID alt-src key deletion status from soln-stage", "satyan@yahoo-inc.com,sakshat@yahoo-inc.com", fileAttList);
		}else{
			List<String> inputFiletoZip = new ArrayList<String>();
			inputFiletoZip.add(resFilePath1);
			inputFiletoZip.add(listFilePath);
			Zipper.makeZip(inputFiletoZip, zipFilePath);
			List<String> fileAttList = new ArrayList<String>();
			fileAttList.add(zipFilePath);
			sendMail("<h3>Sherpa Delete Script has finished execution on soln-stage</h3><br><p>However some errors have been encountered while deleteing entries. TMS alt-src Key's which failed to be deleted are available at 'SherpaDeleteFailureLog.txt', inside the attached zip file, along with the original file which contains all the TMS ID alt-src keys.</p>", "TMS ID alt-src key deletion status from soln-stage", "satyan@yahoo-inc.com,sakshat@yahoo-inc.com", fileAttList);
		}
    }
    
    /**
     * Method to make the HTTP call to delete a sherpa entry
     * @param sherpaURL
     * @param headerValue
     * @return
     */
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
    		LOGGER.error("Fatal protocol violation: " + e.getMessage());
    	    e.printStackTrace();
    	}catch(IOException e){
    		LOGGER.error("Fatal transport error: " + e.getMessage());
    	    e.printStackTrace();
    	}finally{
    		method.releaseConnection();
    	}
    	return responseData;
    }
    
    /**
     * Common utility method to send mails using the mail send program
     * @param bodyPart
     * @param sub
     * @param cclist
     * @param fileAttList
     */
    public static void sendMail(String bodyPart, String sub, String cclist, List<String> fileAttList){
		try
	    {
	      String to="sujitroy@yahoo-inc.com";
	      String cc = cclist;
	      String from="mailbot@yahoo-inc.com";
	      String body=bodyPart+"<br><p>Do not reply to this message.</p>";
	      MailerApp ma = new MailerApp();
		  ma.sendHTMLMail(to, cc, from, sub, body, fileAttList);
	    }
	    catch (Exception ex)
	    {
	      LOGGER.error("Error while sending mail", ex);
	    }
	}
    
    public static int intervalNotify(Calendar startTime, Calendar flagTime){
		int status = 0;
		long start = startTime.getTimeInMillis();
		long flag = flagTime.getTimeInMillis();
		long diff = Math.abs(flag - start);
		long diffInSeconds = diff/1000;
		if(diffInSeconds>=900){
			sendMail("<p>TribuneMedia service alt-src key deletion is still going on for soln-stage ID Assigner Sherpa Table</p>", "TMS ID alt-src key deletion status from soln-stage - still going on", "sakshat@yahoo-inc.com", null);
			status = 1;
		}
		return status;
	}
}
