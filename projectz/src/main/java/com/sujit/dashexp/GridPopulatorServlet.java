package com.sujit.dashexp;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sujit.dashexp.dao.TaskDAO;
import com.sujit.dashexp.datamodel.Task;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class GridPopulatorServlet extends HttpServlet {
	static Logger log = Logger.getLogger(GridPopulatorServlet.class.getName());
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		TaskDAO td = new TaskDAO();
		/* Task delete part for accidental errors in JSON which mandates task deletion to unbock the system */
		//td.deleteTask(Long.valueOf(712));
		//td.deleteTask(Long.valueOf(711));

		String userId = (String)request.getAttribute("userid");
		log.info("|- requesting userid :"+userId);
		
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma","no-cache");
		response.setDateHeader ("Expires", 0);
		log.log(Level.INFO, "|-- Obtaining task listing");
		PrintWriter out = response.getWriter();
		//out.println("[{\"name\" : \"Movies photo and slideshow migration\",\"status\" : \"In-progress\",\"priority\" : \"1\",\"description\" : \"No task details added yet!\",\"taskLastUpdate\" : \"12/08/2011\"},{\"name\" : \"Person headshot photo migration\",\"status\" : \"Initiated\",\"description\" : \"No task details added yet!\",\"priority\" : \"2\",\"taskLastUpdate\" : \"12/08/2011\"},{\"name\" : \"check astro details\",\"status\" : \"Initiated\",\"priority\" : \"10\",\"description\" : \"Details not added yet for this task!\",\"taskLastUpdate\" : \"02/03/2012\"}]");
		//td.deleteTask(Long.valueOf("11"));
		//out.println(CommonUtils.writeTasksInJsonForGrid(td.getAllTasks(userId)));
		
		//String resText = "{\"timer\":\""+ getSyncTime(request) +"\",\"tasks\":"+CommonUtils.writeTasksInJsonForGrid(td.getAllTasks(userId))+"}";
		
		List<Task> userTasks = td.getAllTasks(userId);
		String resText = "{\"timer\":\""+ getSyncTime(request) +"\",\"dueTaskCount\":\""+CommonUtils.getTodaysDueTasksCount(userTasks)+"\",\"tasks\":"+CommonUtils.writeTasksInJsonForGrid(userTasks)+"}";
		
		log.log(Level.INFO, "|-- Line Print:"+resText);
		
		out.println(resText);
		
	}
	
	private String getSyncTime(HttpServletRequest siteRequest){
		HttpSession session = siteRequest.getSession();
		Calendar lastSyncTime = (Calendar)session.getAttribute("lastDBSyncTime");
		Calendar currentTime = Calendar.getInstance();
		String result = "0:0";
		
		if(lastSyncTime!=null){
			log.log(Level.INFO, "|-- Obtained last sync time from session. Calculating the sync interval...");
			long targetTime = lastSyncTime.getTimeInMillis()+3600000;
			long timeDiffInSeconds = (targetTime - currentTime.getTimeInMillis())/1000;
			float fractionalMinutes = timeDiffInSeconds/60;
			long minutes = (long)fractionalMinutes;
			double fractionalSeconds = fractionalMinutes - minutes;
			long seconds = (long)Math.ceil(fractionalSeconds*60);
			result = minutes + ":" + seconds;
		}else{
			log.log(Level.INFO, "|-- Could not get last sync time from session.");
			result = "0:0";
		}
		return result;
	}
}
