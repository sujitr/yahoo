package com.yahoo.tmsutils.eraser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.yahoo.ccm.CCMObject;
import com.yahoo.ccm.serialization.CCMObjectSerializationException;
import com.yahoo.ccm.serialization.json.CCMObjectJSONSerializer;

import org.apache.log4j.Logger;

public class CCMHelper {
	private final static Logger Log = Logger.getLogger(CCMHelper.class);
	/**
	 * get the CCMObject from UUID
	 * reading prod carmot url
	 * @param uuid
	 * @return
	 * @throws Exception
	 */
	public static CCMObject getCCMObjectFromUUID(String uuid, String carmotReadUrl) throws Exception  {
		String ccm = readUrlToString(carmotReadUrl + uuid);
		CCMObjectJSONSerializer jsonSerializer = new CCMObjectJSONSerializer();
		CCMObject ccmObject = jsonSerializer.deserialize(ccm);
		return ccmObject;
	}
	
	/**
	 * read from url into string
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private static String readUrlToString(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) 
            response.append(inputLine);
        in.close();
        return response.toString();
    }
}
