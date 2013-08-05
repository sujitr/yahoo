package com.sujit.yahoo.mailer.SendMail;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailerApp {
	
	private final static Logger LOGGER = Logger.getLogger(MailerApp.class.getName());
	
	private String defaultSMTPServer;
	private String defaultSMTPPort;
	
	public MailerApp(){
		super();
		defaultSMTPServer = "smarthost.yahoo.com";
		defaultSMTPPort = "25";
	}
	
	public MailerApp(String smtpHost, String smtpPort){
		super();
		defaultSMTPServer = smtpHost;
		defaultSMTPPort = smtpPort;
	}
	
    public static void main( String[] args )
    {
        MailerApp ma = new MailerApp();
        ma.sendMail("sujitroy@yahoo-inc.com", null, "sujitroy@yahoo-inc.com", "testsubject", "testbody", null);
    }
    
    public void sendMail(String to, String cc, String from, String subject, String body, List<String> attachementFilePathList){
    	LOGGER.info("Attempting to send mail...");
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", defaultSMTPServer);
			props.put("mail.smtp.port", defaultSMTPPort);
			Session session = Session.getDefaultInstance(props, null);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
			if (cc != null) {
				msg.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(cc, false));
			}
			msg.setSubject(subject);

			// Create the message part
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			Multipart multipart = new MimeMultipart("mixed");
			multipart.addBodyPart(messageBodyPart);

			if (attachementFilePathList!=null) {
				for (String str : attachementFilePathList) {
					MimeBodyPart attBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(str);
					attBodyPart.setDataHandler(new DataHandler(source));
					attBodyPart.setFileName(source.getName());
					multipart.addBodyPart(attBodyPart);
				}
			}
			msg.setContent(multipart);
			msg.setHeader("X-Mailer", "Automated Mailer");
			msg.setSentDate(new Date());
			Transport.send(msg);
			LOGGER.info("Mail message sent successfully.");
		} catch (AddressException e) {
			LOGGER.severe("AddressException : "+e.getMessage());
			e.printStackTrace();
		} catch (MessagingException e) {
			LOGGER.severe("MessagingException : "+e.getMessage());
			e.printStackTrace();
		}
    }
}
