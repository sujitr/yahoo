package com.sujit.dashexp.datamodel;

/**
 * Simple POJO for representing a single user report schedule data model
 * @author sujitroy
 *
 */
public class ReportSchedule {
	private String userId;
	private boolean reportingEnabledFlag;
	private String toAddresses;
	private String ccAddresses;
	private String fromAddress;
	private String reportSubject;
	private String reportScheduleDay;
	private String reportScheduleTime;
	private boolean clubbingEnabledFlag;
	private boolean individualReportFlag;
	private String clubWithUserId;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public boolean getReportingEnabledFlag() {
		return reportingEnabledFlag;
	}
	public void setReportingEnabledFlag(boolean reportingEnabledFlag) {
		this.reportingEnabledFlag = reportingEnabledFlag;
	}
	public String getToAddresses() {
		return toAddresses;
	}
	public void setToAddresses(String toAddresses) {
		this.toAddresses = toAddresses;
	}
	public String getCcAddresses() {
		return ccAddresses;
	}
	public void setCcAddresses(String ccAddresses) {
		this.ccAddresses = ccAddresses;
	}
	public String getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	public String getReportSubject() {
		return reportSubject;
	}
	public void setReportSubject(String reportSubject) {
		this.reportSubject = reportSubject;
	}
	public String getReportScheduleDay() {
		return reportScheduleDay;
	}
	public void setReportScheduleDay(String reportScheduleDay) {
		this.reportScheduleDay = reportScheduleDay;
	}
	public String getReportScheduleTime() {
		return reportScheduleTime;
	}
	public void setReportScheduleTime(String reportScheduleTime) {
		this.reportScheduleTime = reportScheduleTime;
	}
	public boolean getClubbingEnabledFlag() {
		return clubbingEnabledFlag;
	}
	public void setClubbingEnabledFlag(boolean clubbingEnabledFlag) {
		this.clubbingEnabledFlag = clubbingEnabledFlag;
	}
	public String getClubWithUserId() {
		return clubWithUserId;
	}
	public void setClubWithUserId(String clubWithUserId) {
		this.clubWithUserId = clubWithUserId;
	}
	public boolean getIndividualReportFlag() {
		return individualReportFlag;
	}
	public void setIndividualReportFlag(boolean individualReportFlag) {
		this.individualReportFlag = individualReportFlag;
	}
}
