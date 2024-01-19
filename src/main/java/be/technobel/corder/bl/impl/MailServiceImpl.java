package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.services.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * MailServiceImpl is an implementation of the MailService interface.
 * It provides methods for sending emails and building email templates.
 */
@Service
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    public MailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Sends an email.
     *
     * @param to              the recipient's email address
     * @param subject         the subject of the email
     * @param content         the content of the email
     * @param isHtmlContent   true if the email content is HTML, false otherwise
     * @throws MailSendException if there is an error sending the email
     */
    @Override
    public void sendMail(String to, String subject, String content, boolean isHtmlContent){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom("info@corder.be");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtmlContent);
            mailSender.send(message);
        }catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }
    }

    /**
     * Builds an email template by processing the given template with the provided variables.
     *
     * @param template   the email template to be processed
     * @param variables  the map of variables to be used in the template processing
     * @return the built email template as a string
     */
    @Override
    public String buildEmailTemplate(String template, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(template, context);
    }
}
