package com.sujit.dashexp;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.text.ParseException;
import com.sujit.dashexp.datamodel.Task;
import com.sujit.dashexp.datamodel.ReportSchedule;
import java.util.logging.Logger;

/**
 * Class containing utility methods for various purposes.
 * @author sujitroy
 *
 */
public class CommonUtils {
	static Logger log = Logger.getLogger(CommonUtils.class.getName());	
	public static String writeTasksInJsonForGrid(List<Task> tasks){
		StringBuilder sb = new StringBuilder();
		if(tasks!=null && !tasks.isEmpty()){
			sb.append("[");
			for(Task t:tasks){
				sb.append("{");
				sb.append("\"name\" : \"" +displaySafeTags(t.getTaskTitle()) + "\",");
				sb.append("\"status\" : \"" + t.getTaskStatus()+ "\",");
				sb.append("\"priority\" : \"" + t.getTaskPriority()+ "\",");
				sb.append("\"description\" : \"" + t.getTaskDescription()+ "\",");
				sb.append("\"taskLastUpdate\" : \"" + getFormattedTimestamp(t.getTaskLastUpdated())+ "\",");
				sb.append("\"taskBugId\" : \"" + t.getTaskBugId()+ "\",");
				sb.append("\"taskBugPriority\" : \"" + t.getTaskPriorityBugzilla()+ "\",");
				sb.append("\"taskId\" : \"" + t.getTaskId()+ "\",");
				sb.append("\"taskduedate\" : \"" + dueDateFormatter(t.getTaskDueDate().trim())+ "\"");
				sb.append("},");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb.append("]");
		}
		System.out.println(sb.toString());
		return sb.toString();
	}

	public static String writeScheduleInJson(ReportSchedule primaryUserSchedule, ReportSchedule secondaryUserSchedule){
		StringBuilder sb = new StringBuilder();
		if(primaryUserSchedule!=null){
			sb.append("{").append("\"enabledFlag\":"+primaryUserSchedule.getReportingEnabledFlag()+ ",");
			sb.append("\"toAddresses\" : \"" + primaryUserSchedule.getToAddresses()+ "\",");
			sb.append("\"ccAddresses\" : \"" + primaryUserSchedule.getCcAddresses()+ "\",");
			sb.append("\"fromAddress\" : \"" + primaryUserSchedule.getFromAddress()+ "\",");
			sb.append("\"subject\" : \"" + primaryUserSchedule.getReportSubject()+ "\",");
			sb.append("\"sday\" : \"" + primaryUserSchedule.getReportScheduleDay()+ "\",");
			sb.append("\"stime\" : \"" + primaryUserSchedule.getReportScheduleTime()+ "\",");
			sb.append("\"indiFlag\" : " + primaryUserSchedule.getIndividualReportFlag()+ ",");
			sb.append("\"clubFlag\" : " + primaryUserSchedule.getClubbingEnabledFlag()+ ",");
			sb.append("\"clubUser\" : \"" + primaryUserSchedule.getClubWithUserId()+ "\",");
			sb.append("\"clubUserSchedule\" : \"");
			if(secondaryUserSchedule!=null && secondaryUserSchedule.getReportingEnabledFlag()){
				sb.append("scheduled to run on ").append(secondaryUserSchedule.getReportScheduleDay()).append(" at ").append(secondaryUserSchedule.getReportScheduleTime()).append("\"");
			}else{
				sb.append("schedule does not exist or may have been disabled by the user\"");
			}
			sb.append("}");			
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
	
	public static int getTodaysDueTasksCount(List<Task> tasks){
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		int taskCount = 0;
		for(Task t:tasks){
			String taskDueDate = t.getTaskDueDate().trim();
			try {
				Date dueDate = sdf.parse(taskDueDate);
				String tDate = sdf.format(new Date());
				Date todaysDate = sdf.parse(tDate);
				log.info("Todays Date:"+sdf.format(todaysDate)+", and task due date:"+sdf.format(dueDate));
				if(dueDate.equals(todaysDate)){
					if(t.getTaskStatus().toLowerCase().equals("done")){
						log.info("dates are equal but task is also 'Done', so this task is not due.");
					}else{
						log.info("dates are equal but task is not 'Done'");
						taskCount = taskCount + 1 ;
					}
				}else{
					log.info("dates are NOT equal.");
				}
			} catch (ParseException e) {
				//e.printStackTrace();
				log.info("Some exception while reading the date, may be no due date defined for this task!");
				log.severe(e.getMessage());
			}
		}
		return taskCount;
	}
	
	public static String getFormattedTimestamp(Date date){
		  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		  //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		  String result = ""; 
		  if (date!=null) {
			result = sdf.format(date);
		}
		return result;
	  }
	
	public static Date getCurrentCalendarDate(){
		  Calendar cal = Calendar.getInstance();
		  //System.out.println(cal.getTimeZone());
		  //cal.add(Calendar.MILLISECOND, 19800000); // adding IST Offset to UTC default zoned calendar
		  //System.out.println(cal.getTimeZone());
		  return cal.getTime();
	  }
	
	 /**
	   * Method to convert any line feed or new line characters in the input string in a format 
	   * suitable for JSON based storage and transmission.
	   * @param input - String
	   * @return converted JSON friendly String
	   */
	  public static String jsonify(String input){
		  String result = input.replaceAll("\\n", "\\\\n").replaceAll("\\r", "\\\\r").replaceAll("\"", "\\\\\"");
		  //System.out.println(">> Jsonified String:"+result);
		  return result;
	  }
	  
	  public static String displaySafeTags(String input){
		  String result = input.replaceAll("\"", "\\\\\"").replaceAll("<","&lt;").replaceAll(">","&gt;");
		  return result;
	  }
	  
	  public static String getUserIdFromYBYCookie(String ybyCookie){
		  String user = null;
		  if(ybyCookie!=null && !ybyCookie.isEmpty()){
			  StringTokenizer st = new StringTokenizer(URLDecoder.decode(ybyCookie), "&");
			  while(st.hasMoreTokens()){
				  String thisTokenValue = st.nextToken();
				  //System.out.println(thisTokenValue);
				  if(thisTokenValue.startsWith("userid")){
					  user = thisTokenValue.substring(7);
				  }
			  }
		  }
		  return user;
	  }

	public static String dueDateFormatter(String inDate){
		  String outDate = "";
		  SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
		  SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
		  try {
			Date d1 = sdf1.parse(inDate);
			outDate = sdf2.format(d1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("|-- Invalid/Empty input date:"+inDate);
			log.severe(e.getMessage());
		}
		  return outDate;
	  }
}
