package com.sujit.dashexp.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sujit.dashexp.dao.HibernateUtil;
import com.sujit.dashexp.scheduler.artifacts.CronScheduler;

public class ProDashContextListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent event) {
		HibernateUtil.getSessionFactory().close();
	}

	public void contextInitialized(ServletContextEvent arg0) {
		// initializing the scheduler system
		try{
	         CronScheduler objPlugin = new CronScheduler();
	       }
	       catch(Exception e)
	       {
	           e.printStackTrace();
	       }
	}

}
