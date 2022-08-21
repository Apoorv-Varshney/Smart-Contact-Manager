package com.smart.service;

import java.util.Properties;


import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendMail(String to, String subject, String body) {

		boolean result = false;

		String from = "apoorvvarshney04@gmail.com";
		// host for gmail
		String host = "smtp.gmail.com";

		// get System Properties
		Properties properties = System.getProperties();

		// setting information
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// session object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				// use app password generated from google account
				return new PasswordAuthentication("apoorvvarshney04@gmail.com", "dcqmzyhmxktpbxrh");
			}

		});

		session.setDebug(true);

		// compose message
		MimeMessage message = new MimeMessage(session);

		try {

			message.setFrom(from);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			
			//messsage can be text or html type
			message.setContent(body,"text/html");

			// send message
			Transport.send(message);
			System.out.println("mail sent successfully...");

			result = true;

		} catch (Exception e) {
			// e.printStackTrace();
		}

		return result;
	}
}
