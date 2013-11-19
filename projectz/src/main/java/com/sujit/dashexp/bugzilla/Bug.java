package com.sujit.dashexp.bugzilla;

public class Bug {
	private long bugId;
	private String bugTitle;
	private String bugStatus;
	private String bugPriority;
	
	public Bug(){}
	
	public Bug(long bugId, String bugTitle, String bugStatus, String bugProirity) {
		super();
		this.bugId = bugId;
		this.bugTitle = bugTitle;
		this.bugStatus = bugStatus;
		this.bugPriority = bugProirity;
	}
	public long getBugId() {
		return bugId;
	}
	public void setBugId(long bugId) {
		this.bugId = bugId;
	}
	public String getBugTitle() {
		return bugTitle;
	}
	public void setBugTitle(String bugTitle) {
		this.bugTitle = bugTitle;
	}
	public String getBugStatus() {
		return bugStatus;
	}
	public void setBugStatus(String bugStatus) {
		this.bugStatus = bugStatus;
	}

	public String getBugPriority() {
		return bugPriority;
	}

	public void setBugPriority(String bugPriority) {
		this.bugPriority = bugPriority;
	}
	
	
	
}
