package com.yahoo.tmsutils.eraser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.sujit.utility.zipapp.Zipper;
import com.sujit.yahoo.mailer.SendMail.MailerApp;
import com.yahoo.ccm.CCMList;
import com.yahoo.ccm.CCMObject;
import com.yahoo.ccm.Facet;
import com.yahoo.ccm.serialization.json.CCMObjectJSONSerializer;

/**
 * This utility class reads the ID Assigner table sherpa dump (as generated by the dump tool at <a href="http://roadload.corp.gq1.yahoo.com/tools.php">Roadload Machine</a>).
 * It parses the sherpa dump, filters those UUID's which are associated with any tribune media alt-src key. Thereafter, it fetches the CCM's from those UUID's and checks if the movie/venue facet (depending
 * upon the entity type) contains any tribunemedia alt-src keys.<br>
 * If it finds any tribune media alt-src key which has show (id starting with 'SH') or episode (id starting with 'EP')  then it creates another ccm with that facet and self facet, but those alt-src keys removed and write that ccm in a file.
 * This file can be used to post the ccm's again back into carmot.
 * @author sujitroy
 *
 */
public class App {
	private final static Logger Log = Logger.getLogger(App.class);
	private String sherpaDumpFile = "";
	private String workspaceLocation = "";
	private String tribuneUUIDFile = "";
	private String carmotReadUrl = "";
	private String targetAltSrcFile = "";
	private String updatedCCMFile = "";
	private String statisticsFile = "";
	private String ccmBackupFile = "";
	private String altKeyBackupFile = "";
	private String errorRecordFile = "";
	
	/**
	 * Constructor
	 * @param sherpaDumpFile
	 * @param carmotUrl
	 */
	public App(String sherpaDumpFile, String carmotUrl){
		super();
		this.sherpaDumpFile = sherpaDumpFile;
		this.carmotReadUrl = carmotUrl;
		File dumpFile = new File(sherpaDumpFile);
		this.workspaceLocation = dumpFile.getParent();
		this.tribuneUUIDFile = workspaceLocation+File.separator+"TribuneUUIDs.txt"; // this is the file which will contain the filtered/unique UUID's of those records having tribune alt-src keys
		this.targetAltSrcFile = workspaceLocation+File.separator+"AltKeysWhichNeedToBeRemovedFromSherpa.txt"; // this is the file where all filtered tribunemedia alt-src keys (which were removed from ccm's) will be kept
		this.updatedCCMFile = workspaceLocation+File.separator+"UpdatedCCMsToBePostedBack.txt"; // this is the file where all the updated ccm's (baseline_movie/cinemasource_venue facet + self facets) will be placed (one ccm per line)
		this.statisticsFile = workspaceLocation+File.separator+"Statistics.txt"; // this is the file to store the statistics of the operation
		this.ccmBackupFile = workspaceLocation+File.separator+"CCMBackup.txt"; // this is the file to store the backup of the ccm's which are getting updated
		this.altKeyBackupFile = workspaceLocation+File.separator+"AltKeyBackup.txt"; // this is the file to store the backup of the alt-src key to UUID mapping which are getting updated
		this.errorRecordFile = workspaceLocation+File.separator+"ErrorRecord.txt"; // this is the file which will record any kind of error while processing the CCM's, so that they could be re-tried later 
	}
	
	/**
	 * Method to filter out the unique uuid's which are associated with tribune media alt-src keys.
	 * This method uses linux native commands 'egrep', 'cut' and 'sort' for faster performance over large files.
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private void filterTribuneMediaRecords() throws IOException, InterruptedException{
		Log.info("|-- attempting to filter out the sherpa table dump...");
		String[] shellcmd = {"/bin/sh", "-c", "egrep \"tribune\" "+ sherpaDumpFile +" | cut -f1 -d'|' | sort -u"};
		Process p = Runtime.getRuntime().exec(shellcmd);
		Log.info("|-- Native call finished...writing out the results...");
		/*
		 * The file writer component initialization
		 */
		File uuidFile = new File(tribuneUUIDFile);
		if(uuidFile.exists() && uuidFile.delete()){
			uuidFile.createNewFile();
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(uuidFile,true));
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = in.readLine()) != null) {
        	out.write(line);
        	out.newLine();
        }
       out.flush();
       out.close();
       Log.info("|-- filter process completed. UUID file is available at :"+uuidFile.getAbsolutePath());
       Log.info("|-- number of unique UUID's with tribune alt-src keys are :"+getNumberOfLines(uuidFile.getAbsolutePath()));
	}
	
	/**
	 * Method to get the lines count in a file
	 * @param fileName
	 * @return number of lines
	 * @throws IOException
	 */
	private int getNumberOfLines(String fileName) throws IOException{
		LineNumberReader reader = null;
		reader = new LineNumberReader(new FileReader(fileName));
		int cnt = 0;
		String lineRead = "";
		while ((lineRead = reader.readLine()) != null) {}
		cnt = reader.getLineNumber(); 
		reader.close();
		return cnt;
	}
	
	/**
	 * Method to hit the carmot with filtered out UUID's and check for offensive alt-src patterns. If it finds one, then it creates a CCM 
	 * with just that facet (with those alt-src keys removed) and the self facet for post back to carmot
	 * @throws IOException
	 */
	private void processUUIDs() throws IOException{
		Scanner scanner = null;
		List<String> facetNameList = new ArrayList<String>();
		CCMObjectJSONSerializer serializer = new CCMObjectJSONSerializer();
		File targetKeyFile = new File(targetAltSrcFile);
		File postJsonFile = new File(updatedCCMFile);
		File ccmBkpFile = new File(ccmBackupFile);
		File altKeyBkpFile = new File(altKeyBackupFile);
		File errorFile = new File(errorRecordFile);
		// cleanup any old file
		if(targetKeyFile.exists()&&targetKeyFile.delete()){
			targetKeyFile.createNewFile();
		}
		if(postJsonFile.exists()&&postJsonFile.delete()){
			postJsonFile.createNewFile();
		}
		if(ccmBkpFile.exists()&&ccmBkpFile.delete()){
			ccmBkpFile.createNewFile();
		}
		if(altKeyBkpFile.exists()&&altKeyBkpFile.delete()){
			altKeyBkpFile.createNewFile();
		}
		if(errorFile.exists()&&errorFile.delete()){
			errorFile.createNewFile();
		}
		BufferedWriter keyWriter = new BufferedWriter(new FileWriter(targetKeyFile,true));
		BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(postJsonFile,true));
		BufferedWriter ccmBackupWriter = new BufferedWriter(new FileWriter(ccmBkpFile,true));
		BufferedWriter altSrcBackupWriter = new BufferedWriter(new FileWriter(altKeyBkpFile,true));
		BufferedWriter errorWriter = new BufferedWriter(new FileWriter(errorFile,true));
		try {
			scanner = new Scanner(new FileReader(tribuneUUIDFile));
			while (scanner.hasNextLine()) {
				String uuid = scanner.nextLine().trim();
				Log.info("|-- checking for UUID :"+uuid);
				facetNameList.clear();
				CCMObject ccmObject = null;
				try{
					ccmObject = CCMHelper.getCCMObjectFromUUID(uuid, carmotReadUrl);
				}catch(IOException iex){
					errorWriter.write("|-- Error occurred while reading UUID contents :"+iex.getMessage());
					Log.error("|-- Error :"+iex.getMessage(),iex);
				}
				boolean updateFlag=false;
				if (ccmObject != null) {
					String backupCCMString = serializer.serialize(ccmObject);
					Facet selfFact = ccmObject.getIdentity();
					Iterator<Entry<String, Facet>> facets = ccmObject.getFacetsIterator();
					while(facets.hasNext()){
						String facetName = facets.next().getKey();
						facetNameList.add(facetName.trim());
					}
					if(facetNameList.contains("baseline:movie")){
						Facet movieFacet = ccmObject.getFacet("baseline:movie");
						String entityType = movieFacet.getAsString("entity_type");
						if(entityType!=null){
							if(entityType.trim().equals("movie")){
								// do nothing
							}else if(entityType.trim().equals("tv_show") || entityType.trim().equals("tv_episode")){
								// remove the suspected alternate keys
								CCMList altList = movieFacet.getAsCCMList("alternate_key");
								Iterator<Object> altIterator = altList.iterator();
								int targetKeyCounter = 0;
								while(altIterator.hasNext()){
									String altKey = (String)altIterator.next();
									if(altKey.trim().contains("tribunemediaservices.com/program?id=EP")||altKey.trim().contains("tribunemediaservices.com/series?id=SH")||altKey.trim().contains("tribunemediaservices.com/theater?id=")){
										altIterator.remove();
										targetKeyCounter=targetKeyCounter+1;
										// write the key in a file for reference
										keyWriter.write(altKey);
										keyWriter.newLine();
										// write the alt src key in the backup file
										altSrcBackupWriter.write(altKey+"|"+uuid);
										altSrcBackupWriter.newLine();
									}
								}
								Log.info("|-- Number of target tribunemedia keys obtained for UUID "+ uuid +" is :"+targetKeyCounter);
								if (targetKeyCounter>0) {
									// create the post json and write it in a file
									CCMObject postCcm = new CCMObject(ccmObject.getId());
									postCcm.addIdentity(selfFact);
									postCcm.addFacet("baseline:movie",movieFacet);
									String ccmString = serializer.serialize(postCcm);
									jsonWriter.write(ccmString);
									jsonWriter.newLine();
									updateFlag = true;
								}
							}
						}
					}else if(facetNameList.contains("baseline_movie:movie")){
						Facet movieFacet = ccmObject.getFacet("baseline_movie:movie");
						// remove the suspected alternate keys
						CCMList altList = movieFacet.getAsCCMList("alternate_key");
						Iterator<Object> altIterator = altList.iterator();
						int targetKeyCounter = 0;
						while(altIterator.hasNext()){
							String altKey = (String)altIterator.next();
							if(altKey.trim().contains("tribunemediaservices.com/program?id=EP")||altKey.trim().contains("tribunemediaservices.com/series?id=SH")||altKey.trim().contains("tribunemediaservices.com/theater?id=")){
								altIterator.remove();
								targetKeyCounter=targetKeyCounter+1;
								// write the key in a file for reference
								keyWriter.write(altKey);
								keyWriter.newLine();
								// write the alt src key in the backup file
								altSrcBackupWriter.write(altKey+"|"+uuid);
								altSrcBackupWriter.newLine();
							}
						}
						Log.info("|-- Number of taregt tribunemedia keys obtained for UUID "+ uuid +" is :"+targetKeyCounter);
						if (targetKeyCounter>0) {
							// create the post json and write it in a file
							CCMObject postCcm = new CCMObject(ccmObject.getId());
							postCcm.addIdentity(selfFact);
							postCcm.addFacet("baseline_movie:movie", movieFacet);
							String ccmString = serializer.serialize(postCcm);
							jsonWriter.write(ccmString);
							jsonWriter.newLine();
							updateFlag = true;
						}
					}else if(facetNameList.contains("cinemasource:venue")){
						Facet venueFacet = ccmObject.getFacet("cinemasource:venue");
						CCMList altList = venueFacet.getAsCCMList("alternate_key");
						Iterator<Object> altIterator = altList.iterator();
						int targetKeyCounter = 0;
						while(altIterator.hasNext()){
							String altKey = (String)altIterator.next();
							if(altKey.trim().contains("tribunemediaservices.com/theater?id=")){
								altIterator.remove();
								targetKeyCounter=targetKeyCounter+1;
								// write the key in a file for reference
								keyWriter.write(altKey);
								keyWriter.newLine();
								// write the alt src key in the backup file
								altSrcBackupWriter.write(altKey+"|"+uuid);
								altSrcBackupWriter.newLine();
							}
						}
						Log.info("|-- Number of taregt tribunemedia keys obtained for UUID "+ uuid +" is :"+targetKeyCounter);
						if (targetKeyCounter>0) {
							// create the post json and write it in a file
							CCMObject postCcm = new CCMObject(ccmObject.getId());
							postCcm.addIdentity(selfFact);
							postCcm.addFacet("cinemasource:venue", venueFacet);
							String ccmString = serializer.serialize(postCcm);
							jsonWriter.write(ccmString);
							jsonWriter.newLine();
							updateFlag = true;
						}
					}
					if(updateFlag){
						// write the original ccm in the backup file
						ccmBackupWriter.write(backupCCMString);
						ccmBackupWriter.newLine();
					}
				}
			}
		}catch(Exception e){
			Log.error("|-- Error encountered while processing the data :"+e.getMessage(),e);
			errorWriter.write("|-- Error encountered while processing :"+e.getMessage());
			errorWriter.newLine();
		}
		finally{
			keyWriter.flush();
			keyWriter.close();
			jsonWriter.flush();
			jsonWriter.close();
			altSrcBackupWriter.flush();
			altSrcBackupWriter.close();
			ccmBackupWriter.flush();
			ccmBackupWriter.close();
			errorWriter.flush();
			errorWriter.close();
		}
	}
	
	/**
	 * Method to collect the statistics of the entire process
	 * @throws IOException
	 */
	private String extractStatistics() throws IOException{
		StringBuilder plainTextStatistics = new StringBuilder();
		plainTextStatistics.append("========================================================================\n");
		plainTextStatistics.append("====================== Tribune ID Removal Statistics ==========================\n");
		plainTextStatistics.append("========================================================================\n");
		plainTextStatistics.append("Number of records present in the Sherpa Table dump of ID Assigner Table (Input File - '"+ sherpaDumpFile +"') :"+getNumberOfLines(sherpaDumpFile)+"\n");
		plainTextStatistics.append("Number of unique UUID's filtered from the sherpa dump having tribune alt-src keys :"+getNumberOfLines(tribuneUUIDFile)+"\n");
		plainTextStatistics.append("Number of alt-src keys which has been removed from UUID's and which also needs to be deleted from ID Assigner table :"+getNumberOfLines(targetAltSrcFile)+"\n");
		plainTextStatistics.append("Number of CCM's which were updated and needs to be posted :"+getNumberOfLines(updatedCCMFile));
		Log.info("|-- Processing statistics - \n"+plainTextStatistics.toString());
		File statFile = new File(statisticsFile);
		if(statFile.exists()&&statFile.delete()){
			statFile.createNewFile();
		}
		BufferedWriter statWriter = new BufferedWriter(new FileWriter(statFile,false));
		statWriter.write(plainTextStatistics.toString());
		statWriter.newLine();
		statWriter.flush();
		statWriter.close();
		return plainTextStatistics.toString();
	}
	
	/**
	 * Main method to initiate the process
	 * @param args
	 */
    public static void main( String[] args ){
    	if (args.length < 2) {
			Log.error("Please provider the input file location of the Sherpa ID Assigner table dump and Carmot read URL.");
			return;
		} else {
			Log.info("Obtained Sherpa dump file path :" + args[0]);
			Log.info("Obtained carmot read URL :" + args[1]);
			if(args[0]!=null && !args[0].isEmpty() && args[1]!=null && !args[1].isEmpty()){
				App eraseApp = new App(args[0], args[1]);
				MailerApp mailer = new MailerApp();
				Zipper zip = new Zipper();
				try {
					Log.info("|-- Number of lines in the Sherpa Dump :"+eraseApp.getNumberOfLines(eraseApp.sherpaDumpFile));
					eraseApp.filterTribuneMediaRecords();
					eraseApp.processUUIDs();
					if(eraseApp.getNumberOfLines(eraseApp.targetAltSrcFile)>0){
						String statisticsDetail = eraseApp.extractStatistics();
						List<String> filesToBeZipped = new ArrayList<String>();
						//filesToBeZipped.add(eraseApp.tribuneUUIDFile);
						filesToBeZipped.add(eraseApp.targetAltSrcFile);
						filesToBeZipped.add(eraseApp.updatedCCMFile);
						filesToBeZipped.add(eraseApp.statisticsFile);
						//filesToBeZipped.add(eraseApp.altKeyBackupFile);
						//filesToBeZipped.add(eraseApp.ccmBackupFile);
						zip.makeZip(filesToBeZipped, eraseApp.workspaceLocation+File.separator+"TMSRemovalArtifacts.zip");
						List<String> finalZippedList = new ArrayList<String>();
						finalZippedList.add(eraseApp.workspaceLocation+File.separator+"TMSRemovalArtifacts.zip");
						mailer.sendHTMLMail("sujitroy@yahoo-inc.com", null, "mailbot@yahoo-inc.com", "TMS alt-src key removal task execution completed", "<h3>TMS alt-src key removal task execution has been completed</h3><p>"+statisticsDetail+"</p><br><p>Attached files contains the list of alt-src'es removed from CCM's and the updated CCM's which can now be posted. The backup files for CCM's modified and the alt-src key vs UUID's which are to be removed, and the targeted Tribune UUID's are available at the location - "+ eraseApp.workspaceLocation +"</p><hr><h5>This is an automated mail. Please do not reply to this email address.</h5>", finalZippedList);
					}else{
						String statisticsDetail = eraseApp.extractStatistics();
						List<String> filesToBeZipped = new ArrayList<String>();
						filesToBeZipped.add(eraseApp.tribuneUUIDFile);
						filesToBeZipped.add(eraseApp.statisticsFile);
						zip.makeZip(filesToBeZipped, eraseApp.workspaceLocation+File.separator+"TMSRemovalArtifacts.zip");
						List<String> finalZippedList = new ArrayList<String>();
						finalZippedList.add(eraseApp.workspaceLocation+File.separator+"TMSRemovalArtifacts.zip");
						mailer.sendHTMLMail("sujitroy@yahoo-inc.com", null, "mailbot@yahoo-inc.com", "TMS alt-src key removal task execution completed - no suspected alt-src key found", "<h3>TMS alt-src key task execution has been completed</h3><p>However, there are no such alt-src keys which needed to be removed. Attached zip file contains the listing of the UUID's which were processed.</p><hr><h5>This is an automated mail. Please do not reply to this email address.</h5>", finalZippedList);
					}
				}  catch (Exception e) {
					Log.error("|-- Exception encountered: "+e.getMessage(), e);
					mailer.sendPlainTextMail("sujitroy@yahoo-inc.com", null, "mailbot@yahoo-inc.com", "TMS alt-src key removal task execution encountered an error", "Exception was thrown \n"+e.getMessage()+"\nThe stacktrace is - \n"+e.getStackTrace(), null);
				}
			}else{
				Log.info("|-- there is something wrong with the input arguments to the program...please check the details");
				return;
			}
		}
    }
    

}
