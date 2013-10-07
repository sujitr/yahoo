package com.sujit.utils.xmlextractor;

import org.xml.sax.SAXException;

/**
 * Exception class to stop program execution when the specified count limit has been reached
 * @author sujitroy
 *
 */
@SuppressWarnings("serial")
public class FetchLimitExceededException extends SAXException  {
	public FetchLimitExceededException(String message) {
        super(message);
    }
}
