package be.technobel.corder.bl.services;

import java.util.Map;

public interface MailService {
    void sendMail(String to, String subject, String content, boolean isHtmlContent);

    String buildEmailTemplate(String template, Map<String, Object> variables);
}
