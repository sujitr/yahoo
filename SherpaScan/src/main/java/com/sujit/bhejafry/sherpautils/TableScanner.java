package com.sujit.bhejafry.sherpautils;

import java.io.IOException;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.logging.Logger;

public class TableScanner {
	private final static Logger LOGGER = Logger.getLogger(TableScanner.class .getName());
	public static void main( String[] args ) throws IOException {
    	String hostNamePart = "";
    	String ycaHeaderValue = "";
    	String outPutFolderPath = "";
    	if(args.length<2){
    		LOGGER.severe("Not enough input parameters. Need the query URL string,'Yahoo-App-Auth' header value and ID List file location.");
    		return;
    	}else{
    		LOGGER.info("Obtained Query URL :"+args[0]);
    		LOGGER.info("Obtained Header Info :"+args[1]);
    		LOGGER.info("Obtained List File Path :"+args[2]);
    		hostNamePart = args[0];
    		ycaHeaderValue = args[1];
    		outPutFolderPath = args[2];
    	}
    	
    	String startHashKey = "0x00000000" ;
    	boolean scanComplete = false;
    	HttpClient client = new HttpClient();
    	client.getHostConfiguration().setProxy("yca-proxy.corp.yahoo.com", 3128);
    	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outPutFolderPath+File.separator+"ScanResults.txt",true));
    	
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
					bufferedWriter.write(key.trim()+"|"+uuidValue.trim());
					bufferedWriter.newLine();
				}
			} catch (JSONException e) {
				LOGGER.severe("JSON Parse Exception while attempting to parse sherpa response: "+e.getMessage());
			}
			bufferedWriter.flush();
    	}
    	 bufferedWriter.flush();
         bufferedWriter.close();
         HttpConnectionManager mgr = client.getHttpConnectionManager();
         if (mgr instanceof SimpleHttpConnectionManager) {
             ((SimpleHttpConnectionManager)mgr).shutdown();
         }
         LOGGER.info("End of the scanner program.");
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
      
}
