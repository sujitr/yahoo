package com.sujit.dashexp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sujit.dashexp.dao.ReportScheduleDAO;
import com.sujit.dashexp.dao.TaskDAO;
import com.sujit.dashexp.datamodel.ReportSchedule;
import com.sujit.dashexp.mailer.MailerApp;


@SuppressWarnings("serial")
public class ScheduleRecordServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(ScheduleRecordServlet.class.getName());
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession();
		String userId = (String)req.getAttribute("userid");
		if(userId!=null){
			// all good, use that in real production environment
		}else{
			userId = "check1"; // the check userid for test environment
		}
		
		/* get the form attributes */
		String enableFlag = req.getParameter("reportEnable");
		String toAddresses = req.getParameter("to-addresses");
		String ccAddresses = req.getParameter("cc-addresses");
		String fromAddress = req.getParameter("from-address");
		String reportSubject = req.getParameter("subject");
		String scheduleDay = req.getParameter("scheduleDay");
		String scheduleTime = req.getParameter("scheduleTime"); 
		//String scheduleType = req.getParameter("club");
		String[] scheduleTypes = req.getParameterValues("club");
		String scheduleClubUserId = req.getParameter("clb-user");
		
		/* just checking if the values has been captured properly */
		log.info("|- Enable Flag value is :"+enableFlag);
		log.info(toAddresses);
		log.info(ccAddresses);
		log.info(fromAddress);
		log.info(reportSubject);
		log.info(scheduleDay);
		log.info(scheduleTime);
		//log.info(scheduleType);
		if (scheduleTypes!=null) {
			System.out.println("Length of checkbox string array:"+scheduleTypes.length);
			for (String schType : scheduleTypes) {
				log.info("|-- check bx val: "+schType);
			}
		}
		log.info(scheduleClubUserId);
		
		ReportScheduleDAO reportEngine = new ReportScheduleDAO();
		
		if(enableFlag!=null){
			log.info("|-- Enable flag is marked, so these values will be either added or updated in the system...");
			if(reportEngine.isSchedulePresent(userId)){
				// it's an update operation
				log.info("|-- Updating schedule details for "+ userId+" in the database");
				int res = 0;
				/*if(scheduleType!=null && scheduleType.equals("individual")){
					res = reportEngine.updateReportSchedule(userId, true, toAddresses.trim(), ccAddresses.trim(), fromAddress.trim(), reportSubject.trim(), scheduleDay, scheduleTime);
					res = reportEngine.makeScheduleIndividual(userId);
				}else if(scheduleType!=null && scheduleType.equals("clubbed")){
					res = reportEngine.enableReportSchedule(userId);
					res = reportEngine.makeScheduleClubbed(userId, scheduleClubUserId);
				}*/
				if(scheduleTypes!=null){
					res = reportEngine.enableReportSchedule(userId);
					if(scheduleTypes.length==2){
						// do the code for both
						log.info("|-- both the check marks are ticked.");
						res = reportEngine.updateReportSchedule(userId, true, toAddresses.trim(), ccAddresses.trim(), fromAddress.trim(), reportSubject.trim(), scheduleDay, scheduleTime);
						if(scheduleClubUserId!=null && scheduleClubUserId.equals(userId)){
							// do nothing, as it will cause repeat of data in mail
						}else{
							res = reportEngine.makeScheduleClubbed(userId, scheduleClubUserId);
						}
						res = reportEngine.updateIndividualReportingChoice(userId, true);
					}else{
						if(scheduleTypes[0].equals("individual")){
							log.info("|-- only individual checkbox is ticked.");
							res = reportEngine.updateReportSchedule(userId, true, toAddresses.trim(), ccAddresses.trim(), fromAddress.trim(), reportSubject.trim(), scheduleDay, scheduleTime);
							res = reportEngine.makeScheduleDeClubbed(userId);
							res = reportEngine.updateIndividualReportingChoice(userId, true);
						}else{
							log.info("|-- only clubbed checkbox is ticked.");
							if(scheduleClubUserId!=null && scheduleClubUserId.equals(userId)){
								// do nothing, as it will cause repeat of data in mail
								log.info("|-- clubbed user is same as current user. Cannot perform this kind of clubbing.");
							}else{
								res = reportEngine.makeScheduleClubbed(userId, scheduleClubUserId);
							}
							res = reportEngine.updateIndividualReportingChoice(userId, false);
						}
					}
				}
				if(res==1){
					log.info("|-- Schedule update is successful");
					session.setAttribute("message", "Your schedule updated successfully!");
				}
			}else{
				// it's a new schedule creation
				/* need to add null check conditions in case when a new user first time creates a schedule with only clubbed support */
				log.info("|-- Adding schedule details for "+ userId+" in the database");
				int res = 0;
				
				if(scheduleTypes.length==2){
					// do the code for both
					log.info("|-- both the check marks are ticked.");
					res = reportEngine.addSchedule(userId, true, toAddresses.trim(), ccAddresses.trim(), fromAddress.trim(), reportSubject.trim(), scheduleDay, scheduleTime);
					res = reportEngine.updateIndividualReportingChoice(userId, true);
					res = reportEngine.makeScheduleClubbed(userId, scheduleClubUserId);
				}else{
					if(scheduleTypes[0].equals("individual")){
						log.info("|-- only individual checkbox is ticked.");
						res = reportEngine.addSchedule(userId, true, toAddresses.trim(), ccAddresses.trim(), fromAddress.trim(), reportSubject.trim(), scheduleDay, scheduleTime);
						res = reportEngine.updateIndividualReportingChoice(userId, true);
						res = reportEngine.makeScheduleDeClubbed(userId);
					}else{
						log.info("|-- only clubbed checkbox is ticked.");
						res = reportEngine.addSchedule(userId, true, "", "", "", "", "everyday", "09:00AM");
						res = reportEngine.makeScheduleClubbed(userId, scheduleClubUserId);
						res = reportEngine.updateIndividualReportingChoice(userId, false);
					}
				}
				
				if(res==1){
					log.info("|-- Addition of the schedule is successful");
					session.setAttribute("message", "Your schedule created successfully!");
				}
			}
		}else{
			// disable the schedule enable flag in the database
			if(reportEngine.isReportingEnabled(userId)){
				reportEngine.disableReportSchedule(userId);
				log.info("|-- Schedule has been disabled");
				session.setAttribute("message", "Schedule disabled successfully!");
			}else{
				log.info("|-- No actions have been taken as no change specified, reporting was already disabled");
				session.setAttribute("message", "No actions have been taken as no change specified, reporting was already disabled!");
			}
		}
		resp.sendRedirect("/dashboard.jsp");
	}
}
