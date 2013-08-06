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

/**
 * Standard Mailer class to send HTML enabled mails with file attachments. This can be used in any application 
 * to send mails. This class uses the Yahoo! free send mail SMTP host by default, so please use discretion to send huge amount
 * of mails. As they say, since it's free, better to use it judiciously to keep it that way :)
 * Otherwise, if you have access to any other SMTP server then please use that instead of the free one. Thanks!<br><br>
 * Please note that this class has been written to use un-authorized SMTP servers, which does not need a password.
 * This class WON'T work with SMTP servers which need password for authentication.
 * @author sujitroy
 *
 */
public class MailerApp {
	
	private final static Logger LOGGER = Logger.getLogger(MailerApp.class.getName());
	
	private String defaultSMTPServer;
	private String defaultSMTPPort;
	
	/**
	 * Default Constructor - uses the free Yahoo! SMTP server credentials 
	 */
	public MailerApp(){
		super();
		defaultSMTPServer = "smarthost.yahoo.com";
		defaultSMTPPort = "25";
	}
	
	/**
	 * Overloaded constructor to use some other SMTP server credentials 
	 * @param smtpHost Custom SMTP hostname
	 * @param smtpPort Custom SMTP port
	 */
	public MailerApp(String smtpHost, String smtpPort){
		super();
		defaultSMTPServer = smtpHost;
		defaultSMTPPort = smtpPort;
	}
    
    /**
     * Method to send a simple plain text mail with file attachments. The file paths needs to be passed as a list.
     * @param to To email address (can be comma separated)
     * @param cc Cc email address (can be comma separated)
     * @param from Sender email address
     * @param subject Subject of the mail
     * @param body Body of the mail in PlainText
     * @param attachementFilePathList List of file paths on local system
     */
    public void sendPlainTextMail(String to, String cc, String from, String subject, String body, List<String> attachementFilePathList){
    	LOGGER.info("Attempting to send mail...");
		try {
			//Properties props = System.getProperties();
			Properties props = new Properties();
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
    
    /**
     * Method to send a HTML formatted mail with file attachments. You can use free form HTML text as body of the mail. 
     * The file paths needs to be passed as a list.
     * @param to To email address (can be comma separated)
     * @param cc Cc email address (can be comma separated)
     * @param from Sender email address
     * @param subject Subject of the mail
     * @param bodyHTML HTML text for mail body
     * @param attachementFilePathList List of file paths on local system
     */
    public void sendHTMLMail(String to, String cc, String from, String subject, String bodyHTML, List<String> attachementFilePathList){
    	LOGGER.info("Attempting to send mail...");
		try {
			//Properties props = System.getProperties();
			Properties props = new Properties();
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
			messageBodyPart.setContent(bodyHTML, "text/html");
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
