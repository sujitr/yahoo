package com.sujit.bhejafry.sherpautils;

import java.io.IOException;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONException;
import org.json.JSONObject;

import yjava.security.yca.YCAException;
import com.sujit.test.ycatest.YcaReader;
import com.sujit.yahoo.mailer.SendMail.MailerApp;

import org.apache.log4j.Logger;

public class TableScanner {
	private final static Logger LOGGER = Logger.getLogger(TableScanner.class .getName());
	private final static MailerApp mailer = new MailerApp();
	public static void main( String[] args ) throws IOException {
    	String hostNamePart = "";
    	String ycaAppId = "";
    	String outPutFolderPath = "";
    	if(args.length<2){
    		LOGGER.error("Not enough input parameters. Need the query URL string,'Yahoo-App-Auth' header value and ID List file location.");
    		return;
    	}else{
    		LOGGER.info("Obtained Query URL :"+args[0]);
    		LOGGER.info("Obtained Yca App Id :"+args[1]);
    		LOGGER.info("Obtained List File Path :"+args[2]);
    		hostNamePart = args[0];
    		ycaAppId = args[1];
    		outPutFolderPath = args[2];
    	}
    	String ycaHeaderValue = "";
    	String startHashKey = "0x00000000" ;
    	boolean scanComplete = false;
    	HttpClient client = new HttpClient();
    	client.getHostConfiguration().setProxy("yca-proxy.corp.yahoo.com", 3128);
    	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outPutFolderPath+File.separator+"ScanResults.txt",true));
    	String sherpaTableName = hostNamePart.substring(hostNamePart.indexOf("hash_scan/")+10, hostNamePart.length()-1);
    	try {
			ycaHeaderValue = YcaReader.getYcaCertificateValue(ycaAppId);
			LOGGER.info("YCA Header obtained is:"+ycaHeaderValue);
		} catch (YCAException e1) {
			e1.printStackTrace();
		}
    	
    	if(ycaHeaderValue==null || ycaHeaderValue.isEmpty()){
    		LOGGER.info("Unable to obtain yca app certificate value for the given app-id on this machine. Please check the yca-appid details.");
    		return;
    	}
    	Calendar startTime = Calendar.getInstance();
    	while(!scanComplete){
    		String sherpaResponse = hitSherpa(hostNamePart, ycaHeaderValue, startHashKey, client);
    		try {
				JSONObject sherpaResponseJson = new JSONObject(sherpaResponse);
				// get the completed flag
				scanComplete = sherpaResponseJson.getJSONObject("ydht").getJSONObject("continuation").getBoolean("scan_completed");
				if(!scanComplete){
					// get the continuation part
					String nextHashKey = sherpaResponseJson.getJSONObject("ydht").getJSONObject("continuation").getString("start_hash_key");
					LOGGER.info("Next set of hashkey :"+nextHashKey);
					startHashKey = nextHashKey;
				}else{
					LOGGER.info("Detected end of scan signal from Sherpa...");
				}
				// get the records part
				JSONObject sherpaRecords = sherpaResponseJson.getJSONObject("ydht").getJSONObject("records");
				for(String key : JSONObject.getNames(sherpaRecords)){
					LOGGER.info("Key :"+key);
					String uuidValue = sherpaRecords.getJSONObject(key).getJSONObject("fields").getJSONObject("UUID").getString("value");
					//String recordValue = sherpaRecords.getJSONObject(key).getJSONObject("fields").toString();
					bufferedWriter.write(key.trim()+"|"+uuidValue.trim());
					bufferedWriter.newLine();
				}
			} catch (JSONException e) {
				LOGGER.error("JSON Parse Exception while attempting to parse sherpa response: "+e.getMessage(),e);
			}
			bufferedWriter.flush();
			Calendar flagTime = Calendar.getInstance();
       	  	int status = intervalNotify(startTime, flagTime);
       	  	if(status == 1){
       	  		startTime = Calendar.getInstance();
       	  	}
    	}
    	 bufferedWriter.flush();
         bufferedWriter.close();
         HttpConnectionManager mgr = client.getHttpConnectionManager();
         if (mgr instanceof SimpleHttpConnectionManager) {
             ((SimpleHttpConnectionManager)mgr).shutdown();
         }
         LOGGER.info("End of the ID Assigner Sherpa Table scanner program.");
         mailer.sendPlainTextMail("sujitroy@yahoo-inc.com",null, "mailbot@yahoo-inc.com", "Sherpa table scan for ID Assigner table "+sherpaTableName+" has finished", "Sherpa Scan for ID assigner table "+ sherpaTableName +" has finished successfully. Dump is located at '"+outPutFolderPath+File.separator+"ScanResults.txt'.\nPlease do not reply to this mail id", null);
    }
    	
    
    /**
     * Method to hot the Sherpa Table and get the JSON response
     * @param sherpaURL
     * @param headerValue
     * @return JSON
     */
    @SuppressWarnings("deprecation")
	public static String hitSherpa(String sherpaURL, String headerValue, String startHashKey, HttpClient client){
    	String responseData = "";
        PostMethod method = new PostMethod(sherpaURL);
    	Header yHeader = new Header("Yahoo-App-Auth",headerValue);
    	Header hashKeyHeader = new Header("x-dht-start-hash-key",startHashKey);
    	method.addRequestHeader(yHeader);
    	method.addRequestHeader(hashKeyHeader);
    	method.setRequestBody(createPostBody(startHashKey));
    	try{
    		int statusCode = client.executeMethod(method);
    		if (statusCode != HttpStatus.SC_OK) {
    	        LOGGER.info("Method failed: " + method.getStatusLine());
    	    }
    		 responseData = method.getResponseBodyAsString();
    		 LOGGER.info("HTTP Response is = "+responseData);
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
     * Create a new post body with a new start hash key
     * @param startHashKey
     * @return
     */
    public static String createPostBody(String startHashKey){
    	StringBuilder sb = new StringBuilder();
    	sb.append("{\"ydht\": {\"continuation\": {\"start_hash_key\": \"");
    	sb.append(startHashKey);
    	sb.append("\",\"end_hash_key\": \"0xFFFFFFFF\"},\"record_limit\": 100,\"byte_limit\": 2097152}}");
    	return sb.toString();
    }
    
    
    public static int intervalNotify(Calendar startTime, Calendar flagTime){
		int status = 0;
		long start = startTime.getTimeInMillis();
		long flag = flagTime.getTimeInMillis();
		long diff = Math.abs(flag - start);
		long diffInSeconds = diff/1000;
		if(diffInSeconds>=900){
			mailer.sendPlainTextMail("sujitroy@yahoo-inc.com", null, "mailbot@yahoo-inc.com", "Sherpa Scan is still going on. Will update once it's over", "Sherpa Scan is still going on. Will update once it's over. \nPlease do not reply to this mail id", null);
			status = 1;
		}
		return status;
	}
      
}
