package com.sujit.dashexp.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sujit.dashexp.CommonUtils;
import com.sujit.dashexp.bugzilla.Bug;
import com.sujit.dashexp.bugzilla.ResponseParser;
import com.sujit.dashexp.dao.TaskDAO;
import com.sujit.dashexp.datamodel.Task;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DBSyncFilter implements Filter {
	private static final Logger log = Logger.getLogger(DBSyncFilter.class.getName());
	
	private FilterConfig filterConfig = null;

	public void destroy() {
		this.filterConfig = null;
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest siteRequest = (HttpServletRequest)req;
		HttpServletResponse siteResponse = (HttpServletResponse) res;
		HttpSession session = siteRequest.getSession(true);
		/* get the YBY cookie */
		String ybyCookie = "";
		Cookie[] cookies = siteRequest.getCookies();
		for (Cookie thisCookie : cookies){
			 if(thisCookie.getName().equals("YBY")){
				 ybyCookie = "YBY="+thisCookie.getValue();
			 }
		}
		if(session.isNew()){
			log.info("|- New session. DB Sync will be performed.");
			syncDB(ybyCookie);
			session.setAttribute("lastDBSyncTime", Calendar.getInstance());
			session.setMaxInactiveInterval(21600);
		}else{
			Calendar lastSyncTime = (Calendar)session.getAttribute("lastDBSyncTime");
			if(lastSyncTime!=null){
				Calendar currentTime = Calendar.getInstance();
				long timeDiffInMinutes = ((currentTime.getTimeInMillis() - lastSyncTime.getTimeInMillis())/(60*1000));
				log.info("|- Time difference since last DB Sync = "+timeDiffInMinutes+" minutes");
				if(timeDiffInMinutes>10){
					log.info("|- Its been over ten minutes since last sync, so DB sync will be performed");
					syncDB(ybyCookie);
					session.setAttribute("lastDBSyncTime", Calendar.getInstance());
				}else{
					log.info("|- Its been less than ten minutes since last sync, so DB sync will NOT be performed");
				}
			}else{
				log.info("|- could not find last DB sync info in session. Sync will be forced.");
				syncDB(ybyCookie);
				session.setAttribute("lastDBSyncTime", Calendar.getInstance());
			}
		}
		filterChain.doFilter(siteRequest, siteResponse);
	}

	public void init(FilterConfig fc) throws ServletException {
		this.filterConfig = fc;
	}
	
	private void syncDB(String ybyCookie) throws IOException{
		/*
		 * [Remember : DB Sync is a costly operation. Use it only when required.]
		 * 1. get the yby cookie for this user
		 * 2. query bugzilla to get bug ids for this user (list A)
		 * 3. query local db to get all tasks where there is a bugid exists for this user. (list B)
		 * 4. compare the lists of bug ids.
		 * 5. if list A has some bug ids which is not available in list B then add those bugs in local DB
		 * 6. if List A does not have some bug ids which is available in List B then delete those bugs in local DB.
		 */
		ResponseParser rp = new ResponseParser();
		TaskDAO taskDao = new TaskDAO();
		HashMap<Long, Long> bugMapping = new HashMap<Long, Long>();
		String currentUserId = CommonUtils.getUserIdFromYBYCookie(ybyCookie);
		log.info("|- Attempting to sync DB for user:"+currentUserId);
		if(ybyCookie!=null && !ybyCookie.isEmpty()){
			List<Bug> bugZillaBugList =  rp.getAllOpenBugsForUser(ybyCookie);
			List<Task> localTaskList = taskDao.getAllTasks(currentUserId);
			ArrayList<Long> bugZillaIdList = new ArrayList<Long>();
			ArrayList<Long> localBugIdList = new ArrayList<Long>();
			// create a list of only bugzilla bug id's
			for(Bug b: bugZillaBugList){
				bugZillaIdList.add(b.getBugId());
			}
			// create a list of bugzilla bug id's for tasks which are present in the local database
			for(Task t: localTaskList){
				if(t.getTaskBugId()!=null && t.getTaskBugId()>0){
					localBugIdList.add(t.getTaskBugId());
					bugMapping.put(t.getTaskBugId(), t.getTaskId());
				}
			}
			Set<Long> bugsToBeAddedInLocalDB = new HashSet<Long>(bugZillaIdList);
			bugsToBeAddedInLocalDB.removeAll(localBugIdList);
			log.info("|- Bugs which need to be added to local db :"+bugsToBeAddedInLocalDB);
			Set<Long> bugsToBeRemovedFromLocalDB = new HashSet<Long>(localBugIdList);
			bugsToBeRemovedFromLocalDB.removeAll(bugZillaIdList);
			log.info("|- Bugs which need to be removed from local db :"+bugsToBeRemovedFromLocalDB);
			if(bugsToBeAddedInLocalDB!=null && bugsToBeAddedInLocalDB.size()>0){
				// call the DAO method to add these bugs from the bug list for this user
				for(Bug b : bugZillaBugList ){
					if(bugsToBeAddedInLocalDB.contains(b.getBugId())){
						taskDao.addTask(currentUserId, b.getBugTitle(), "Initiated", 10, "Details not added yet for this task!", CommonUtils.getCurrentCalendarDate(), b.getBugId(), b.getBugPriority()); 
					}
				}
			}
			if(bugsToBeRemovedFromLocalDB!=null && bugsToBeRemovedFromLocalDB.size()>0){
				// call the DAO method to delete the tasks associated with these bugs from local db for this user.
				for(Long n:bugsToBeRemovedFromLocalDB){
					Long markedTaskId = bugMapping.get(n);
					taskDao.deleteTask(markedTaskId);
				}
			}
			// synchronize any bug title update in bugzilla for existing bugs
			localTaskList.clear();
			localTaskList =  taskDao.getAllTasks(currentUserId);
			for(Task t : localTaskList){
				if (t.getTaskBugId()!=null && t.getTaskBugId()>0) {
					String oldTaskTitle = t.getTaskTitle();
					log.info("|-- Old Bug Title:" + oldTaskTitle);
					String newTaskTitle = "";
					for (Bug b : bugZillaBugList) {
						if (t.getTaskBugId() == b.getBugId()) {
							newTaskTitle = b.getBugTitle();
						}
					}
					log.info("|-- New Bug Title:" + newTaskTitle);
					if (oldTaskTitle.equals(newTaskTitle)) {
						// do nothing
					} else {
						// update the task title with new title
						log.info("|-- Updating task with new title :"
								+ newTaskTitle);
						taskDao.updateTask(t.getTaskId(), newTaskTitle,
								t.getTaskStatus(), t.getTaskPriority(),
								t.getTaskDescription(), t.getTaskDueDate());
					}
				}
			}
		}else{
			log.info("|- Unable to perform DB sync as invalid YBY cookie is recieved.");
		}
		
	}

}
