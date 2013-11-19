package com.sujit.dashexp.datamodel;

import java.util.Date;

/**
 * Class to represent a basic unit of storage or a record.
 * It is essentially a task assigned for a particular user. It could be 
 * an arbitrary task entered by the user or a task indicated 
 * by BugZilla as a Ticket or Bug.
 * 
 * @author sujitroy
 *
 */
public class Task {
	
	private String userId;											/* Yahoo! Backyard User Id */
	private Long taskId;												/* Internal Task Id */
	private String taskTitle;										/* Internal Task title OR BugZilla Bug Summary */
	private Date taskCreationDate;						/* Internal Task creation date OR BugZilla Bug creation date */
	private int taskPriority;										/* Internal Task priority */
	private String taskStatus;									/* Internal Task status OR BugZilla Bug status */
	private Long taskBugId;										/* BugZilla Bug Id */
	private String taskDescription;						/* Internal Task description */
	//private Long[] relatedBugs;								/* Related BugZilla bug Id's for Internal Tasks OR BugZilla Bug's */
	private Date taskLastUpdated;						/* Internal Task last update date OR BugZilla Bug last comment date */
	private String taskPriorityBugzilla;				/* Priority of task from bugzilla */
	private String taskDueDate;								/* task due date */	
	
	public Task() {
		// empty constructor
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getTaskTitle() {
		return taskTitle;
	}

	public void setTaskTitle(String taskTitle) {
		this.taskTitle = taskTitle;
	}

	public Date getTaskCreationDate() {
		return taskCreationDate;
	}

	public void setTaskCreationDate(Date taskCreationDate) {
		this.taskCreationDate = taskCreationDate;
	}

	public int getTaskPriority() {
		return taskPriority;
	}

	public void setTaskPriority(int taskPriority) {
		this.taskPriority = taskPriority;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public Long getTaskBugId() {
		return taskBugId;
	}

	public void setTaskBugId(Long taskBugId) {
		this.taskBugId = taskBugId;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	/*public Long[] getRelatedBugs() {
		return relatedBugs;
	}

	public void setRelatedBugs(Long[] relatedBugs) {
		this.relatedBugs = relatedBugs;
	}*/

	public Date getTaskLastUpdated() {
		return taskLastUpdated;
	}

	public void setTaskLastUpdated(Date taskLastUpdated) {
		this.taskLastUpdated = taskLastUpdated;
	}

	public String getTaskPriorityBugzilla() {
		return taskPriorityBugzilla;
	}

	public void setTaskPriorityBugzilla(String taskPriorityBugzilla) {
		this.taskPriorityBugzilla = taskPriorityBugzilla;
	}
	
	public String getTaskDueDate() {
		return (taskDueDate==null)?"":taskDueDate;
	}

	public void setTaskDueDate(String taskDueDate) {
		this.taskDueDate = taskDueDate;
	}
	
}
