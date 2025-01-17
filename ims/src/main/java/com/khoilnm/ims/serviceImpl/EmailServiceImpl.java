package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.common.EmailTemplate;
import com.khoilnm.ims.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    public static final String DEFAULT_SENDER = "minhkhoilenhat04@gmail.com";

    public EmailServiceImpl(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }


    /**
     * @param to          String
     * @param subject     String
     * @param template    EmailTemplate
     * @param props       Map<String, Object>
     * @param multiThread boolean
     */
    @Override
    public void sendEmail(String to, String subject, EmailTemplate template, Map<String, Object> props, boolean multiThread) throws MessagingException {
        if (multiThread) {
            new Thread(() -> {
                try {
                    send(subject, to, template, props);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } else {
            send(subject, to, template, props);
        }
    }

    private void send(String subject, String to, EmailTemplate emailTemplate,
                      Map<String, Object> props) throws MessagingException {
        String templateName = emailTemplate.getName();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name());

        Context context = new Context();
        context.setVariables(props);

        helper.setFrom(DEFAULT_SENDER);
        helper.setTo(to);
        helper.setSubject(subject);

        String template = templateEngine.process(templateName, context);

        helper.setText(template, true);

        mailSender.send(mimeMessage);
    }
}
