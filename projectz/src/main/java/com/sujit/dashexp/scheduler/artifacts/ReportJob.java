package com.sujit.dashexp.scheduler.artifacts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.log4j.Level;
import org.eclipse.jetty.util.log.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.sujit.dashexp.dao.ReportScheduleDAO;
import com.sujit.dashexp.dao.TaskDAO;
import com.sujit.dashexp.datamodel.ReportSchedule;
import com.sujit.dashexp.datamodel.Task;
import com.sujit.dashexp.mailer.MailerApp;

public class ReportJob implements Job{
	static Logger log = Logger.getLogger(ReportJob.class.getName());
	/**
	 * Method to fetch the schedule details for all users in the table and send out mails as per schedule settings
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		List<ReportSchedule> schedulesToBeExecutedNow = new ArrayList<ReportSchedule>(); 
		TimeZone indiaTimeZone = TimeZone.getTimeZone("IST");
		Calendar currentCalendar = Calendar.getInstance(indiaTimeZone);
		String currentTimeString = null;
		SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
		currentTimeString = sdf.format(currentCalendar.getTime());
		log.info("|-- current time of the system is :"+currentTimeString);
		log.info("|-- Quartz Scheduler application is running in Jetty....probing the database for matching schedules...");
		
		ReportScheduleDAO reportEngine = new ReportScheduleDAO();
		List<ReportSchedule> allActiveSchedules = reportEngine.getAllActiveSchedules();
		if(allActiveSchedules!=null && allActiveSchedules.size()>0){
			Log.info("|-- Total number of active schedules found:"+allActiveSchedules.size());
			for(ReportSchedule rs:allActiveSchedules){
				log.info("|-- Schedule created by user:"+rs.getUserId());
				String schDay = rs.getReportScheduleDay();
				String schTime = rs.getReportScheduleTime();
				log.info("|-- Schedule is set on run on :"+schDay+" , and at:"+schTime);
				if(schDay.toLowerCase().equals("everyday")){
					if(rs.getReportScheduleTime().toUpperCase().equals(currentTimeString)){
						log.info("|-- Schedule for user:"+rs.getUserId()+" will be executed now");
						schedulesToBeExecutedNow.add(rs);
					}
				}else if(schDay.toLowerCase().equals("monday")){
					if(currentCalendar.get(Calendar.DAY_OF_WEEK)==2){
						if(rs.getReportScheduleTime().toUpperCase().equals(currentTimeString)){
							log.info("|-- Schedule for user:"+rs.getUserId()+" will be executed now");
							schedulesToBeExecutedNow.add(rs);
						}
					}
				}else if(schDay.toLowerCase().equals("tuesday")){
					if(currentCalendar.get(Calendar.DAY_OF_WEEK)==3){
						if(rs.getReportScheduleTime().toUpperCase().equals(currentTimeString)){
							log.info("|-- Schedule for user:"+rs.getUserId()+" will be executed now");
							schedulesToBeExecutedNow.add(rs);
						}
					}
				}else if(schDay.toLowerCase().equals("wednesday")){
					if(currentCalendar.get(Calendar.DAY_OF_WEEK)==4){
						if(rs.getReportScheduleTime().toUpperCase().equals(currentTimeString)){
							log.info("|-- Schedule for user:"+rs.getUserId()+" will be executed now");
							schedulesToBeExecutedNow.add(rs);
						}
					}
				}else if(schDay.toLowerCase().equals("thursday")){
					if(currentCalendar.get(Calendar.DAY_OF_WEEK)==5){
						if(rs.getReportScheduleTime().toUpperCase().equals(currentTimeString)){
							log.info("|-- Schedule for user:"+rs.getUserId()+" will be executed now");
							schedulesToBeExecutedNow.add(rs);
						}
					}
				}else if(schDay.toLowerCase().equals("friday")){
					if(currentCalendar.get(Calendar.DAY_OF_WEEK)==6){
						if(rs.getReportScheduleTime().toUpperCase().equals(currentTimeString)){
							log.info("|-- Schedule for user:"+rs.getUserId()+" will be executed now");
							schedulesToBeExecutedNow.add(rs);
						}
					}
				}
			}
		}
		if(schedulesToBeExecutedNow!=null && schedulesToBeExecutedNow.size()>0){
			log.info("|-- attempting to get schedule for '"+schedulesToBeExecutedNow.size()+"' user(s) which matches current time....");
			MailerApp mapp = new MailerApp();
			for(ReportSchedule trs : schedulesToBeExecutedNow){
				log.info("|-- getting all tasks which are created/updated in last week for user:"+trs.getUserId());
				if(trs.getClubbingEnabledFlag() && !trs.getIndividualReportFlag()){
					log.info("|-- This users schedule is marked to be only clubbed with user '"+ trs.getClubWithUserId() +"'. Not sending any mail in this case.");
				}else{
					String mailBody = getFormattedScheduleMailBody(trs,true);
					if (mailBody!=null && !mailBody.isEmpty()) {
						log.info("|-- sending schedule mail for user:"+ trs.getUserId());
						System.out.println(mailBody);
						mapp.sendHTMLMail(trs.getToAddresses(),
								trs.getCcAddresses(), trs.getFromAddress(),
								trs.getReportSubject(), mailBody, null);
					}else{
						log.info("|-- there were no tasks for user '"+trs.getUserId()+"' which was either created or updated within last week");
						mapp.sendPlainTextMail(trs.getUserId().trim()+"@yahoo-inc.com", null, trs.getFromAddress(), trs.getReportSubject(), "Auto Mailer Update - There were no tasks for you which was either created or updated within last week. So there was no schedule to be sent.", null);
					}
				}
			}
		}
	}
	
	/**
	 * Method to get the html formatted message body for a given user and time range
	 * @param userId
	 * @return
	 */
	private String getFormattedSchedule(String userId, Calendar currCal, Calendar lastCal){
		StringBuilder sb = new StringBuilder("<h4 style='font-family:calibri' >Key Highlights (").append(userId).append(")</h4><ol style='font-family:calibri'>");
		int validTaskCount=0;
		TaskDAO td = new TaskDAO();
		List<Task> allTasks = td.getAllTasks(userId);
		for(Task t : allTasks){
			Calendar taskCreationCal = Calendar.getInstance();
			Calendar taskLastUpdateCal = Calendar.getInstance();
			taskCreationCal.setTime(t.getTaskCreationDate());
			taskLastUpdateCal.setTime(t.getTaskLastUpdated());
			log.info("|-- Current date:"+currCal.getTime()+" AND Task creation date:"+taskCreationCal.getTime());
			if((taskCreationCal.before(currCal) && taskCreationCal.after(lastCal)) || (taskLastUpdateCal.before(currCal) && taskLastUpdateCal.after(lastCal))){
				log.info("|-- Task with title:"+t.getTaskTitle()+" is within the time limit, so it will be reported");
				validTaskCount=validTaskCount+1;
				sb.append("<li>"+t.getTaskTitle());
				if((t.getTaskDescription()!=null || !t.getTaskDescription().isEmpty()) && !t.getTaskDescription().equalsIgnoreCase("Details not added yet for this task!")){
					sb.append("<ul><li>"+replacewithBR(t.getTaskDescription())+"</li></ul>"); 
				}else{
					sb.append("</li>");
				}
			}
		}
		sb.append("</ol>");
		if(allTasks.size()>0 && validTaskCount>0){
			return sb.toString();
		}else{
			return null;
		}
	}
	
	private String getFormattedScheduleMailBody(ReportSchedule mrs, boolean isClubReportNeeded){
		StringBuilder mailBody = new StringBuilder("<h3 style='font-family:calibri' >Weekly Report");
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy");
		Calendar currCal = Calendar.getInstance();
		Calendar lastCal = Calendar.getInstance();
		lastCal.add(Calendar.DAY_OF_MONTH, -7);
		mailBody.append(" - from "+sdf.format(lastCal.getTime())+" till "+sdf.format(currCal.getTime())).append("</h3><hr>");
		// get own schedule
		String ownSchedule = getFormattedSchedule(mrs.getUserId(), currCal, lastCal);
		// get clubbed schedule
		StringBuilder toClubSchedules = new StringBuilder();
		if (isClubReportNeeded) {
			ReportScheduleDAO rdao = new ReportScheduleDAO();
			List<ReportSchedule> clubSchedules = rdao.getAllSchedulesWhoHaveClubbedWithUser(mrs.getUserId());
			for (ReportSchedule crs : clubSchedules) {
				String clubSchedule = new String();
				clubSchedule = getFormattedSchedule(crs.getUserId(), currCal,	lastCal);
				if (clubSchedule != null && !clubSchedule.isEmpty()) {
					toClubSchedules.append(clubSchedule).append("<br>");
				}
			}
		}
		// pack them in a single mail body and return
		if(ownSchedule!=null){
			mailBody.append(ownSchedule);
		}
		if((ownSchedule==null || ownSchedule.isEmpty()) && toClubSchedules.toString().isEmpty()){
			return null;
		}else{
			mailBody.append("<br>").append(toClubSchedules.toString());
			mailBody.append("<br>");
			mailBody.append("<table border='0' style='font-family:calibri' width='100%'><tr><td width='70%'><hr></td><td width='5%' align='center'>EOM</td><td width='10%'><hr></td></tr></table>");
			return mailBody.toString();
		}
	}
	
	private String replacewithBR(String codeRevUrl) {
		System.out.println("replacewithBR Formatter Input:"+codeRevUrl);
		if ((codeRevUrl !=null) && (!(codeRevUrl.isEmpty()))) {
			String repUrl = codeRevUrl.replaceAll("\\\\r\\\\n", "<br>"); 
			System.out.println("replacewithBR Formatter Output:"+repUrl);
			return repUrl;
		}else{
			return "";
		}
	
	}
}
