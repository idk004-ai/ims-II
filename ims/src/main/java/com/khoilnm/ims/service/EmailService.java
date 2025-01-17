package com.khoilnm.ims.service;

import com.khoilnm.ims.common.EmailTemplate;
import jakarta.mail.MessagingException;

import java.util.Map;

public interface EmailService {
    void sendEmail(String to, String subject, EmailTemplate template, Map<String, Object> props, boolean multiThread) throws MessagingException;
}
