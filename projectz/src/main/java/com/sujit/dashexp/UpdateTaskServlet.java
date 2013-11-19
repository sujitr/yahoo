package com.sujit.dashexp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.sujit.dashexp.dao.TaskDAO;


@SuppressWarnings("serial")
public class UpdateTaskServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(UpdateTaskServlet.class.getName());
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession();
		String taskMode = req.getParameter("mode");
		String taskName = req.getParameter("o.taskName");
		String taskStatus = req.getParameter("o.status");
		String taskpriority = req.getParameter("o.priority");
        String taskDescription = req.getParameter("o.desc");
        String taskId = req.getParameter("o.taskId");
	String taskDueDate = req.getParameter("o.duedate").trim();
        PrintWriter out = resp.getWriter();
        log.info( "|-- Attempting to udpate taskId  '"+taskId+"' with mode '"+taskMode+"'");
        TaskDAO taskDao = new TaskDAO();
        
        if(taskMode.equals("editUpdate")){
        	 try {
        		 taskDao.updateTask(Long.valueOf(taskId),taskName, taskStatus, Integer.parseInt(taskpriority), taskDescription, taskDueDate);
                 log.info("|-- Task updated.");
                 session.setAttribute("message", "Task updated successfully");
               } catch (Exception e) {
                 e.printStackTrace();
                 session.setAttribute("message", "Error while updating task! "+e.getMessage());
               }
        }else if(taskMode.equals("editDelete")){
        	log.info("|-- Marking this task with id '"+ taskId +"' for deletion");
        	try{
        		taskDao.deleteTask(Long.valueOf(taskId));
        		log.info("|-- Task deleted.");
        		session.setAttribute("message", "Task deleted successfully");
        	}catch(Exception e){
        		log.info("|-- Unable to delete task.");
        		e.printStackTrace();
        		session.setAttribute("message", "Error while deleting task! "+e.getMessage());
        	}
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
