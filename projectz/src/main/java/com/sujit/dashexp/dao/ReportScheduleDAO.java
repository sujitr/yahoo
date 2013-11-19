package com.sujit.dashexp.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jetty.util.log.Log;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.sujit.dashexp.datamodel.ReportSchedule;

/**
 * DAO class for Report Schedule
 * @author sujitroy
 *
 */
public class ReportScheduleDAO {
	
	static Logger log = Logger.getLogger(ReportScheduleDAO.class.getName());
	
	/**
	 * Method to check if the reporting is enabled for a particular user
	 * @param userId
	 * @return boolean flag value
	 */
	public boolean isReportingEnabled(String userId) throws HibernateException{
		boolean result = false;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        /*List<ReportSchedule> rSchedule = session.createQuery("from ReportSchedule as schedule where schedule.userId='"+userId+"'").list();
        if(rSchedule!=null && rSchedule.size()==1){
        	ReportSchedule userSchedule = rSchedule.get(0);
        	if(userSchedule.getReportingEnabledFlag()){
        		result = true;
        	}
        }*/
        ReportSchedule rs = (ReportSchedule)session.createQuery("from ReportSchedule as schedule where schedule.userId='"+userId+"'").uniqueResult();
        session.getTransaction().commit();
        if(rs !=null && rs.getReportingEnabledFlag()){
        	result = true;
        }
		return result;
	}
	
	/**
	 * Method to check if any schedule is recorded in the database for the given user
	 * @param userId
	 * @return
	 * @throws HibernateException
	 */
	public boolean isSchedulePresent(String userId) throws HibernateException{
		boolean result = false;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        ReportSchedule rs = (ReportSchedule)session.createQuery("from ReportSchedule as schedule where schedule.userId='"+userId+"'").uniqueResult();
        if(rs !=null){
        	result = true;
        }
        session.getTransaction().commit();
		return result;
	}
	
	/**
	 * Method to retrieve a schedule for a particular user
	 * @param userId
	 * @return ReportSchedule
	 */
	public ReportSchedule getSchedule(String userId){
		log.info("|-- Querying to get the schedule for user:"+userId);
		ReportSchedule rs = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try {
			rs = (ReportSchedule)session.createQuery("from ReportSchedule as schedule where schedule.userId='"+userId+"'").uniqueResult();
		} catch (HibernateException e) {
			e.printStackTrace();
			log.severe("|-- No Unique results found for the query...checking for multiple results...looping through the multiple results for the userid:"+userId);
			List<ReportSchedule> rssl = session.createQuery("from ReportSchedule as schedule where schedule.userId='"+userId+"'").list();
			for(ReportSchedule rch : rssl){
				log.info(rch.getReportSubject());
				log.info(rch.getReportScheduleDay());
				log.info(rch.getReportScheduleTime());
				log.info("-----------------------------------------------------------------------------------------------------------------------------");
			}
		}
		session.getTransaction().commit();
		return rs;
	}
	
	/**
	 * Method to insert a schedule for a particular user
	 * @param userId
	 * @param enabledFlag
	 * @param toAddress
	 * @param ccAddress
	 * @param fromAddress
	 * @param reportSubject
	 * @param reportScheduleDay
	 * @param reportScheduleTime
	 * @return int result of the query
	 */
	public int updateReportSchedule(String userId, boolean enabledFlag, String toAddresses, String ccAddresses, String fromAddress, String reportSubject, String reportScheduleDay, String reportScheduleTime){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("update ReportSchedule set reportingEnabledFlag=:reportingEnabledFlag, toAddresses=:toAddresses, ccAddresses=:ccAddresses, fromAddress=:fromAddress, reportSubject=:reportSubject, reportScheduleDay=:reportScheduleDay, reportScheduleTime=:reportScheduleTime where userId=:userId");
        query.setParameter("userId",userId);
        query.setParameter("reportingEnabledFlag",enabledFlag);
        query.setParameter("toAddresses",toAddresses);
        query.setParameter("ccAddresses",ccAddresses);
        query.setParameter("fromAddress",fromAddress);
        query.setParameter("reportSubject",reportSubject);
        query.setParameter("reportScheduleDay",reportScheduleDay);
        query.setParameter("reportScheduleTime",reportScheduleTime);
        int result = query.executeUpdate();
        session.getTransaction().commit();
		return result;
	}
	
	/**
	 * Method to disable reporting for a particular user
	 * @param userId
	 * @return
	 */
	public int disableReportSchedule(String userId){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("update ReportSchedule set reportingEnabledFlag=:reportingEnabledFlag where userId=:userId");
        query.setParameter("reportingEnabledFlag",false);
        query.setParameter("userId",userId);
        int result = query.executeUpdate();
        session.getTransaction().commit();
		return result;
	}
	
	
	/**
	 * Method to enable reporting for a particular user
	 * @param userId
	 * @return
	 */
	public int enableReportSchedule(String userId){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("update ReportSchedule set reportingEnabledFlag=:reportingEnabledFlag where userId=:userId");
        query.setParameter("reportingEnabledFlag",true);
        query.setParameter("userId",userId);
        int result = query.executeUpdate();
        session.getTransaction().commit();
		return result;
	}
	
	public int addSchedule(String userId, boolean enabledFlag, String toAddress, String ccAddress, String fromAddress, String reportSubject, String reportScheduleDay, String reportScheduleTime){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        ReportSchedule rs = new ReportSchedule();
        rs.setUserId(userId);
        rs.setToAddresses(toAddress);
        rs.setFromAddress(fromAddress);
        rs.setReportingEnabledFlag(enabledFlag);
        rs.setCcAddresses(ccAddress);
        rs.setReportScheduleDay(reportScheduleDay);
        rs.setReportScheduleTime(reportScheduleTime);
        rs.setReportSubject(reportSubject);
        session.save(rs);
        session.getTransaction().commit();
        return 1;
	}
	
	/**
	 * Method to delete the schedule for a user
	 * @param userId
	 * @return int result of the query
	 */
	public int deleteSchedule(String userId){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("delete ReportSchedule where userId=:userId");
        query.setParameter("userId",userId);
        int result = query.executeUpdate();
        session.getTransaction().commit();
		return result;
	}
	
	/**
	 * Method to obtain all active schedules presently residing in the database
	 * @return List of Schedule
	 * @throws HibernateException
	 */
	public List<ReportSchedule> getAllActiveSchedules() throws HibernateException{
		List<ReportSchedule> allSchedule = new ArrayList<ReportSchedule>();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        allSchedule = session.createQuery("from ReportSchedule as schedule where schedule.reportingEnabledFlag=true").list();
        session.getTransaction().commit();
        Log.info("|-- Number of schdules present in the database as of now:"+allSchedule.size());
        return allSchedule;
	}
	
	/**
	 * Make a schedule club with another users schedule
	 * @param userId
	 * @param clubWithUserId
	 * @return integer
	 * @throws HibernateException
	 */
	public int makeScheduleClubbed(String userId, String clubWithUserId) throws HibernateException{
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("update ReportSchedule set clubbingEnabledFlag=:clubbingEnabledFlag, clubWithUserId=:clubWithUserId where userId=:userId");
        query.setParameter("userId",userId);
        query.setParameter("clubbingEnabledFlag",true);
        query.setParameter("clubWithUserId",clubWithUserId);
        int result = query.executeUpdate();
        session.getTransaction().commit();
		return result;
	}
	
	/**
	 * Makes a schedule independent (unclub)
	 * @param userId
	 * @return integer
	 * @throws HibernateException
	 */
	public int makeScheduleDeClubbed(String userId) throws HibernateException{
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("update ReportSchedule set clubbingEnabledFlag=:clubbingEnabledFlag, clubWithUserId=:clubWithUserId where userId=:userId");
        query.setParameter("userId",userId);
        query.setParameter("clubbingEnabledFlag",false);
        query.setParameter("clubWithUserId","");
        int result = query.executeUpdate();
        session.getTransaction().commit();
		return result;
	}
	
	/**
	 * Method to update the individual reporting status, by either true or false
	 * @param userId
	 * @param flagValue
	 * @return
	 */
	public int updateIndividualReportingChoice(String userId, boolean flagValue){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("update ReportSchedule set individualReportFlag=:individualReportFlag where userId=:userId");
        query.setParameter("userId",userId);
        query.setParameter("individualReportFlag",flagValue);
        int result = query.executeUpdate();
        session.getTransaction().commit();
		return result;
	}
	
	public List<ReportSchedule> getAllSchedulesWhoHaveClubbedWithUser(String userId){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        List<ReportSchedule> rssl = session.createQuery("from ReportSchedule as schedule where schedule.clubWithUserId='"+userId+"' and schedule.clubbingEnabledFlag=true and schedule.reportingEnabledFlag=true").list();
        session.getTransaction().commit();
        Log.info("|-- Number of schedules which need to be clubbed:"+rssl.size());
        return rssl;
	}

}
