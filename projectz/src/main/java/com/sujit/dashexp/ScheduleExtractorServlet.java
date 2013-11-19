package com.sujit.dashexp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sujit.dashexp.dao.ReportScheduleDAO;
import com.sujit.dashexp.datamodel.ReportSchedule;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class ScheduleExtractorServlet extends HttpServlet {
	static Logger log = Logger.getLogger(ScheduleExtractorServlet.class.getName());
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		ReportScheduleDAO rd = new ReportScheduleDAO();
		String userId = (String)request.getAttribute("userid");
		log.info("|- schedule requesting userid :"+userId);
		String resText = "";
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma","no-cache");
		response.setDateHeader ("Expires", 0);
		log.log(Level.INFO, "|-- Obtaining schedule for this user:"+userId);
		PrintWriter out = response.getWriter();
		// plumbing code - to be removed - start
		// rd.makeScheduleClubbed("check1", "sujitroy");
		// rd.updateIndividualReportingChoice("sujitroy", true);
		// rd.updateIndividualReportingChoice("sakshat", true);
		// rd.updateIndividualReportingChoice("smeghana", true);
		// rd.updateIndividualReportingChoice("smanpr", true);
		// plumbing code - to be removed - end
		ReportSchedule primaryUserSchedule = rd.getSchedule(userId);
		ReportSchedule secondaryUserSchedule = null;
		if(primaryUserSchedule!=null){
			if(primaryUserSchedule.getClubbingEnabledFlag()){
				secondaryUserSchedule = rd.getSchedule(primaryUserSchedule.getClubWithUserId());
				resText = CommonUtils.writeScheduleInJson(primaryUserSchedule, secondaryUserSchedule);
			}else{
				resText = CommonUtils.writeScheduleInJson(primaryUserSchedule, null);
			}
		}else{
			log.log(Level.INFO, "|-- No schedule found for this user:"+userId);
		}
		out.println(resText);
	}
}
