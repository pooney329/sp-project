package com.test.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String otpCode) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo(to);
        helper.setSubject("[쇼핑몰] 이메일 인증 코드");
        helper.setText(
            "<p>아래 인증 코드를 입력해 주세요 (유효시간 5분).</p>" +
            "<h2 style='letter-spacing:4px'>" + otpCode + "</h2>",
            true
        );
        mailSender.send(message);
    }
}
