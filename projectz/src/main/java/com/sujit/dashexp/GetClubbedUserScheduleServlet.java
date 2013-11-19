package com.sujit.dashexp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sujit.dashexp.dao.ReportScheduleDAO;
import com.sujit.dashexp.datamodel.ReportSchedule;

public class GetClubbedUserScheduleServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(GetClubbedUserScheduleServlet.class.getName());
	public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
		HttpSession session = req.getSession();
		String userId = (String)req.getAttribute("userid");
		String clubUserId = req.getParameter("q");
		log.info("|-- Clubbed User ID request obtained:"+clubUserId);
		ReportScheduleDAO reportEngine = new ReportScheduleDAO();
		StringBuilder sb = new StringBuilder();
		ReportSchedule rs = null;
		if(clubUserId!=null && !clubUserId.isEmpty()){
			rs = reportEngine.getSchedule(clubUserId);
			if(rs!=null && rs.getReportingEnabledFlag()){
				sb.append("For '"+clubUserId+"', reporting is scheduled on '"+rs.getReportScheduleDay()+"' at "+rs.getReportScheduleTime());
			}else{
				sb.append("User '"+clubUserId+"' may not have any existing schedule or it might be disabled.");
			}
		}
		String result = sb.toString();
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma","no-cache");
		response.setDateHeader ("Expires", 0);
		PrintWriter out = response.getWriter();
		if(result!=null && !result.isEmpty()){
			out.println(result);
		}else{
			out.println("No data found for this user!");
		}
	}
}
