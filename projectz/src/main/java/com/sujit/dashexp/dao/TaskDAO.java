package com.sujit.dashexp.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import java.util.*;
import java.util.logging.Logger;

import com.sujit.dashexp.CommonUtils;
import com.sujit.dashexp.dao.HibernateUtil;
import com.sujit.dashexp.datamodel.Task;

public class TaskDAO {
	static Logger log = Logger.getLogger(TaskDAO.class.getName());
	
	// method to create a task
	public int addTask(String taskUser, String taskTitle, String taskStatus, int taskPriority, String taskDescription, Date taskDate, Long taskBugId, String taskPriorityBugzilla){
		 Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	        session.beginTransaction();
	        Task newTask = new Task();
	        newTask.setUserId(taskUser);
	        newTask.setTaskTitle(taskTitle);
	        newTask.setTaskStatus(taskStatus);
	        newTask.setTaskDescription(taskDescription);
	        newTask.setTaskPriority(taskPriority);
	        newTask.setTaskCreationDate(taskDate);
	        newTask.setTaskLastUpdated(taskDate);
	        newTask.setTaskBugId(taskBugId);
		newTask.setTaskPriorityBugzilla(taskPriorityBugzilla);
	        session.save(newTask);
	        session.getTransaction().commit();
	        return 1;
	}
	
	// method to get all tasks in the database
	public List<Task> getAllTasks(String userId){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        List<Task> result = session.createQuery("from Task as task where task.userId='"+userId+"' order by task.taskId DESC").list();
        session.getTransaction().commit();
        for(Task t:result){
        	log.info("TaskName: "+t.getTaskTitle()+ " TaskID:"+t.getTaskId() + " Task Status:"+ t.getTaskStatus());
        }
        return result;
	}
	
	public int updateTask(Long taskId, String taskName, String taskStatus, int taskPriority, String taskDescription, String taskDueDate){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("update Task set taskStatus = :taskStatus, taskPriority = :taskPriority, taskTitle=:taskTitle , taskDescription = :taskDescription, taskLastUpdated = :taskLastUpdated, taskDueDate = :taskDueDate where taskId = :thisTaskId");
        query.setParameter("thisTaskId",taskId);
        query.setParameter("taskStatus",taskStatus);
        query.setParameter("taskPriority",taskPriority);
        query.setParameter("taskTitle",taskName);
        query.setParameter("taskDescription",CommonUtils.jsonify(taskDescription));
        query.setParameter("taskLastUpdated",CommonUtils.getCurrentCalendarDate());
	query.setParameter("taskDueDate",taskDueDate);
        int result = query.executeUpdate();
        session.getTransaction().commit();
		return result;
	}
	
	public int deleteTask(Long deleteTaskId){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("delete Task where taskId = :thisTaskId");
        query.setParameter("thisTaskId",deleteTaskId);
        int result = query.executeUpdate();
        session.getTransaction().commit();
		return result;
	}
}
