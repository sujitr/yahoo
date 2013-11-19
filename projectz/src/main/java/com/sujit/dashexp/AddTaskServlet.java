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
import com.sujit.dashexp.dao.TaskDAO;


@SuppressWarnings("serial")
public class AddTaskServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(AddTaskServlet.class.getName());
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession();
		String newTask = req.getParameter("task");
		String userId = (String)req.getAttribute("userid");
        if (newTask == null || newTask.isEmpty()) {
            log.info("No task specified to be added!");
            return;
        }
        log.log(Level.INFO, "Creating Task '"+newTask+"' for user :"+userId);
        TaskDAO taskEngine = new TaskDAO();
        
        try {
            taskEngine.addTask(userId, newTask.trim(), "Initiated", 10, "Details not added yet for this task!", CommonUtils.getCurrentCalendarDate(),Long.parseLong("0"), null);
            log.info("Task created.");
            session.setAttribute("message", "Task created successfully!");
          } catch (Exception e) {
            log.severe("Exception:"+e.getMessage());
          }
          
	  resp.sendRedirect("/dashboard.jsp");
	  /*
          RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/dashboard.jsp");
          try {
			dispatcher.forward( req, resp );
		} catch (ServletException e) {
			e.printStackTrace();
		}*/
	}
}
