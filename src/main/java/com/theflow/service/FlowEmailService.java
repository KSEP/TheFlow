package com.theflow.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Stas
 */
@Service
public class FlowEmailService {
    
    private static final String FROM = "tflow.mail@taskflow.com";
    
    @Autowired
    JavaMailSenderImpl mailSender;

    @Transactional
    public void sendEmail(String to, String body) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mailMsg = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mailMsg.setFrom(FROM);
        mailMsg.setTo(to);
        mailMsg.setSubject("Flow notification");
        mailMsg.setText(body, true);
        mailSender.send(mimeMessage);
    }
}