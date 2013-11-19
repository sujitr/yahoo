package com.sujit.dashexp.bugzilla;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import org.jdom.xpath.XPath;

import java.util.logging.Level;
import java.util.logging.Logger; 

import com.sujit.dashexp.CommonUtils;

import yjava.byauth.jaas.HttpClientBouncerAuth;

public class ResponseParser {
	
	private static final Logger log = Logger.getLogger(ResponseParser.class.getName());
	
	public List<Bug> getAllOpenBugsForUser(String requestersYBYCookieString) throws IOException{
		
		/* get YBY cookie value as input in this method.
		 * hit the bugzilla to get wssid first (with that cookie) .
		 * ----------------------------------------------------------------------------------------------------------------
		 * There is an IMPORTANT update here. I am using my own YBY cookie 
		 * and not the YBY cookie from the user. This was done to prevent the 
		 * YBY IP MISMATCH error. The YBY cookie should have identical IP from 
		 * the origination system and the requesting system. So the one generated
		 * from the browser of the user will not match when my code at the linux
		 * box requests with to bugzilla.
		 */
		 
		 String myYBYCookieString = getMyYBYCookie();
		
		String wssId = getWssIDFromBugZilla(myYBYCookieString);
		
		/* Then using that wssid, get the XML string (list of bugs)| actually later I found out that while calling the redirected bugzilla URL, there is no need for
		 * wssid, if we are putting the yby cookie in header  and call the redirected URL directly
		 */
		
		InputStream is = getBugZillaXML(wssId, myYBYCookieString, requestersYBYCookieString);
		
		// then remove the doctype tag...and do normal processing.
		
		/*BufferedReader br = new BufferedReader(new InputStreamReader(is));
    	String line;
    	while ((line = br.readLine()) != null) {
    		System.out.println(line);
    	} 
		br.close();*/
		
		List<Bug> mybugs = getAllOpenBugs(is);
		for(Bug b:mybugs){
			log.info("Bug ID:"+b.getBugId()+ "| Bug Desc:"+ b.getBugTitle() +" | Bug Priority:"+b.getBugPriority());
		}
		return mybugs;
	}
	
	/**
	 * Method to fetch WSSID from Bugzilla service
	 * @param myYBYCookieString
	 * @return WSSID String
	 */
	private String getWssIDFromBugZilla(String myYBYCookieString){
		String wssId = null;
		log.info("Attempting to hit bugzilla api with YBY Cookie :"+myYBYCookieString);
		try{
			//URL wsUrl = new URL("http://api.sandbox.bug.corp.yahoo.com:4080/api/1/wssid/xml");
			URL wsUrl = new URL("http://api.bug.corp.yahoo.com:4080/api/1/wssid/xml");
			HttpURLConnection wsUrlConnection = (HttpURLConnection)wsUrl.openConnection();
			wsUrlConnection.setRequestProperty("Cookie", myYBYCookieString);
			wsUrlConnection.connect();
			 if (wsUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
	               log.info("Connection successful. Response is 200. Reading the WSSID...");
	               InputStream responseStream = removeDtdElement(wsUrlConnection.getInputStream());
	               SAXBuilder builder = new SAXBuilder();
	       			Document doc = (Document) builder.build(responseStream);
	       			XPath x = XPath.newInstance("/bugzilla/new_wssid/WSSID");
	       			Element e = (Element)x.selectSingleNode(doc);
	       			wssId = e.getValue();
	       			log.info("WSSID obtained from Bugzilla:"+wssId);
	       			responseStream.close();
	            } else {
	               log.info("There has been some issue with bugzilla connection. Response is :"+wsUrlConnection.getResponseCode());
					BufferedReader in = new BufferedReader(new InputStreamReader(wsUrlConnection.getInputStream()));
	               	String decodedString;
					while ((decodedString = in.readLine()) != null) {
							   System.out.println(decodedString);
					}
					in.close();
	            }
		}catch(MalformedURLException mex){
			mex.printStackTrace();
		}catch(IOException iex){
			iex.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return wssId;
	}
	
	/**
	 * Method to get the bug list in XML format from the bugzilla API service.
	 * @param wssId
	 * @param myYBYCookieString
	 * @return
	 */
	private InputStream getBugZillaXML(String wssId, String myYBYCookieString, String requestersYBYCookieString){
		InputStream bis = null;
		String userId = CommonUtils.getUserIdFromYBYCookie(requestersYBYCookieString);
		if(wssId !=null && !wssId.isEmpty()){
			//String hitUrl = "http://api.bug.corp.yahoo.com:4080/api/1/bugs/search/xml/namedsearch=Tickets%20I%20own/"+wssId;
			String hitUrl = "http://bug.corp.yahoo.com/buglist.cgi?cmdtype=dorem&ctype=xml&named_query_user="+userId.trim()+"@yahoo-inc.com&columnlist=all&namedcmd=Tickets%20I%20own&convert_to_utc=1&callback=1&remaction=run";
			//String hitUrl = "http://api.bug.corp.yahoo.com/buglist.cgi?cmdtype=dorem&ctype=xml&convert_to_utc=1&callback=1&remaction=run&columnlist=all&namedcmd=Tickets%20I%20own";
			log.info("Attempting to hit bugzilla api with URL :"+hitUrl);
			try{
				URL wsUrl = new URL(hitUrl);
				HttpURLConnection.setFollowRedirects(true);
				HttpURLConnection wsUrlConnection = (HttpURLConnection)wsUrl.openConnection();
				wsUrlConnection.setRequestProperty("Cookie", myYBYCookieString);
				wsUrlConnection.connect();
				 if (wsUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					 log.info("Connection successful. Response is 200. Reading bugs...");
					 bis = removeDtdElement(wsUrlConnection.getInputStream());
				 }else{
					 log.info("Error connecting to the bugzilla service. Response is:"+wsUrlConnection.getResponseCode());
				 }
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			log.severe("WSSID is null or empty string. Could not get bug XML.");
		}
		return bis;
	}
	
	/**
	 * Method to obtain a list of all open bugs currently assigned to an user
	 * @param bugZillaXMLResponse
	 * @return List - list of Bug objects
	 */
	private List<Bug> getAllOpenBugs(InputStream bugZillaXMLResponse){
		List<Bug> bugList = new ArrayList<Bug>();
		SAXBuilder builder = new SAXBuilder();
		try{
			Document doc = (Document) builder.build(bugZillaXMLResponse);
			Element rootNode = doc.getRootElement();
			Element buglist = rootNode.getChild("buglist");
			@SuppressWarnings("unchecked")
			List<Element> list = buglist.getChildren("bug");
			for(Element e:list){
				Bug bug = new Bug(Long.valueOf(e.getChild("bug_id").getValue()),e.getChild("short_short_desc").getValue(),e.getChild("bug_status").getValue(), e.getChild("priority").getValue());
				bugList.add(bug);
			}
			bugZillaXMLResponse.close();
		}catch(JDOMException jex){
			jex.printStackTrace();
		}catch(IOException ioex){
			ioex.printStackTrace();
		}
		return bugList;
	}
	
	/**
	 * Method to clean up the DOCTYPE declarations from the InputStream
	 * @param dataIn InputStream
	 * @return InputStream
	 * @throws IOException
	 */
	private InputStream removeDtdElement(InputStream dataIn) throws IOException{
    	BufferedReader br = new BufferedReader(new InputStreamReader(dataIn));
    	StringBuilder sb = new StringBuilder();
    	String line;
    	while ((line = br.readLine()) != null) {
    		sb.append(line);
    	} 
		br.close();
		String cleanedString = sb.toString().replaceAll("<!DOCTYPE(.*?)>", "");
		return new ByteArrayInputStream(cleanedString.getBytes());
	}
	
	private String getMyYBYCookie(){
		String bouncerUrl = "https://bouncer.by.corp.yahoo.com/login/";
		String userid = "sujitroy";
		/* Need to take out the password field from here to some RSA encrypted config file (with public key based read here )*/
		String passwd = "Welcome@123";
		try {
					HttpClientBouncerAuth byauth = new HttpClientBouncerAuth();
					String cookie = byauth.authenticate(bouncerUrl, userid, passwd.toCharArray());
					log.info("Sujit's cookie obtained is = "+cookie);
					return cookie;
		} catch(IOException e){
			log.severe("IOException while getting Sujit's YBYCookie from Bouncer.");
			return null;
		}
	}

}
