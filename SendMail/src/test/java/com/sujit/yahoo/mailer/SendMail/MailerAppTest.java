package com.sujit.yahoo.mailer.SendMail;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class MailerAppTest {

	@Test
	public void testSendPlainTextMail() {
		MailerApp app = new MailerApp();
		app.sendPlainTextMail("sujitroy@yahoo-inc.com", null, "sujitroy@yahoo-inc.com", "testsubject", "testbody", null);
		
	}

	@Test
	public void testSendHTMLMail() {
		MailerApp app = new MailerApp();
		app.sendHTMLMail("sujitroy@yahoo-inc.com", null, "sujitroy@yahoo-inc.com", "testsubject", "<h2>Test Subject</h2><br><hr>", null);
	}

}
