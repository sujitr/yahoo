package com.sujit.utility.zipapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

/**
 * Utility class for creating a simple zip file out of a bunch of files. 
 * Files must be provided as a list of filepath names to the utility method of the class.
 * @author sujitroy
 *
 */
public class Zipper {
	static Logger log = Logger.getLogger(Zipper.class);
	/**
	 * Utility method to zip files. 
	 * @param filePathList
	 * 	list of filepath names
	 * @param zipFilePath
	 * 	filepath to the output zip file
	 */
	public static void makeZip(List<String> filePathList, String zipFilePath){
		log.debug("attempting to make zip file of the given set of files...");
		byte[] buf = new byte[1024];
		try{
		    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilePath));
		    for(String filePath : filePathList){
		    	FileInputStream in = new FileInputStream(filePath);
		    	File a = new File(filePath);
		    	out.putNextEntry(new ZipEntry(a.getName()));
		    	int len;
		        while ((len = in.read(buf)) > 0) {
		            out.write(buf, 0, len);
		        }
		        out.closeEntry();
		        in.close();
		    }
		    out.close();
		    log.debug("zip file creation completed sucessfully.");
		}catch (Exception e) {
			log.error("Error occurred while creating zip file", e);
		}
	}
}