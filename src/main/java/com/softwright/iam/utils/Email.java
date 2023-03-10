package com.softwright.iam.utils;

import com.softwright.iam.models.EmailTemplate;
import com.softwright.iam.models.MailRequest;
import com.softwright.iam.models.MailResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Email {

    private final static Logger _logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public void send(String to, EmailTemplate template, Optional<String> link, Optional<String> notificationBody) throws Exception {
        _logger.log(Level.INFO,  "send email begin");
        if(to.isEmpty()
                || (template.equals(EmailTemplate.RESET_LINK) && link.isEmpty())
                || (template.equals(EmailTemplate.NOTIFICATION) && notificationBody.isEmpty())) {
            throw new Exception("Invalid Parameters");
        }
        String html = "";
        String uri = "https://mail.softwright.in/sendgrid";
        String from = "support@softwright.in";
        String subject = "Reset Request";
        String text = "Reset Link ";
        try {
            html = template.equals(EmailTemplate.RESET_LINK) ?
                    resetTemplate(link.get()) :
                    notificationTemplate(notificationBody.get());
            text += template.equals(EmailTemplate.RESET_LINK) ?
                    link.get() :
                    notificationTemplate(notificationBody.get());
        } catch (NoSuchElementException ex) {
            _logger.log(Level.WARNING, "send email --> link or notification body value not avl.");
            throw new Exception(ex);
        }
        RestTemplate emailReq = new RestTemplate();
        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        MailRequest body =  new MailRequest(to, from, subject, html, text);
        HttpEntity<MailRequest> entity = new HttpEntity(body, headers);
        try {
            ResponseEntity<MailResponse> response = emailReq.exchange(uri, HttpMethod.POST, entity, MailResponse.class);
            _logger.log(Level.INFO,  "send email --> response " + response);
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "send email --> http error");
            throw new Exception(ex);
        }
        _logger.log(Level.INFO,  "send email done");
    }

    private String resetTemplate(String resetLink) {
        return "<!doctype html><html lang=\"en-US\"><head> <meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\"/> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\"> <title>Single Sign-On</title> <meta name=\"description\" content=\"Single Sign-On\"> <style type=\"text/css\"> a:hover{text-decoration: underline !important;}</style></head><body marginheight=\"0\" topmargin=\"0\" marginwidth=\"0\" style=\"margin: 0px; background-color: #fff8e1;\" leftmargin=\"0\"> <table cellspacing=\"0\" border=\"0\" cellpadding=\"0\" width=\"100%\" bgcolor=\"#fff8e1\" style=\"font-family: 'Open Sans', sans-serif;\"> <tr> <td> <table style=\"background-color: fff8e1; max-width:670px; margin:0 auto;\" width=\"100%\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\"> <tr> <td style=\"height:80px;\">&nbsp;</td></tr><tr> <td style=\"text-align:center;\"> <a href=\"https://opensourcedit.com/sso\" target=\"_blank\" style=\"color: #455056; text-decoration: none;\">Single Sign-On</span> </td></tr><tr> <td style=\"height:20px;\">&nbsp;</td></tr><tr> <td> <table width=\"95%\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:670px;background:#fff; border-radius:3px; text-align:center;-webkit-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);-moz-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);box-shadow:0 6px 18px 0 rgba(0,0,0,.06);\"> <tr> <td style=\"height:40px;\">&nbsp;</td></tr><tr> <td style=\"padding:0 35px;\"> <h1 style=\"color: #ffab00; font-weight:500; margin:0;font-size:32px;font-family:'Rubik',sans-serif;\"> Reset Request </h1> <span style=\"display:inline-block; vertical-align:middle; margin:29px 0 26px; border-bottom:1px solid #cecece; width:100px;\"></span> <p style=\"color:#455056; font-size:15px;line-height:24px; margin:0; text-align: left;\"> Hi! <br><br>You recently requested a link to reset your password. If you initiated this request, use this <a href=\""+resetLink+"\"target=\"_blank\">link</a> or click the Reset Password button below to reset your password. <br><br><b>If you did not initiate this request, please report using this <a href=\"https://opensourcedit.com/sso\" target=\"_blank\">link</a>.</b> <br><br>Thank you! <br><br> <small style=\"color: #455056;\">Note: This link will expire in 5 minutes.</small> </p><a href=\""+resetLink+"\"target=\"_blank\" style=\"background:#ffab00;text-decoration:none !important; font-weight:500; margin-top:35px; color:#fff; font-size:18px; letter-spacing: 1px; padding:10px 24px;display:inline-block;border-radius:50px;\"> Reset Password </a> </td></tr><tr> <td style=\"height:40px;\">&nbsp;</td></tr></table> </td><tr> <td style=\"height:20px;\">&nbsp;</td></tr><tr> <td style=\"text-align:center;\"> <p style=\"font-size:14px; color:rgba(69, 80, 86, 0.7411764705882353); line-height:18px; margin:0 0 0;\"> &copy; softwright.in </p></td></tr><tr> <td style=\"height:80px;\">&nbsp;</td></tr></table> </td></tr></table></body></html>";
    }

    private String notificationTemplate(String notificationBody) {
        return "<!doctype html><html lang=\"en-US\"><head> <meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\"/> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\"> <title>Single Sign-On</title> <meta name=\"description\" content=\"Single Sign-On\"> <style type=\"text/css\"> a:hover{text-decoration: underline !important;}</style></head><body marginheight=\"0\" topmargin=\"0\" marginwidth=\"0\" style=\"margin: 0px; background-color: #e8f5e9;\" leftmargin=\"0\"> <table cellspacing=\"0\" border=\"0\" cellpadding=\"0\" width=\"100%\" bgcolor=\"#e8f5e9\" style=\"font-family: 'Open Sans', sans-serif;\"> <tr> <td> <table style=\"background-color: #e8f5e9; max-width:670px; margin:0 auto;\" width=\"100%\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\"> <tr> <td style=\"height:80px;\">&nbsp;</td></tr><tr> <td style=\"text-align:center;\"> <a href=\"https://opensourcedit.com/sso\" target=\"_blank\" style=\"color: #455056; text-decoration: none;\">Single Sign-On</span> </td></tr><tr> <td style=\"height:20px;\">&nbsp;</td></tr><tr> <td> <table width=\"95%\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:670px;background:#fff; border-radius:3px; text-align:center;-webkit-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);-moz-box-shadow:0 6px 18px 0 rgba(0,0,0,.06);box-shadow:0 6px 18px 0 rgba(0,0,0,.06);\"> <tr> <td style=\"height:40px;\">&nbsp;</td></tr><tr> <td style=\"padding:0 35px;\"> <h1 style=\"color: #00c853; font-weight:500; margin:0;font-size:32px;font-family:'Rubik',sans-serif;\"> Notification </h1> <span style=\"display:inline-block; vertical-align:middle; margin:29px 0 26px; border-bottom:1px solid #cecece; width:100px;\"></span> <p style=\"color:#455056; font-size:15px;line-height:24px; margin:0; text-align: left;\"> Hi! <br><br>"+notificationBody+"<br><br><b>If you did not perform this action, please report using this <a href=\"https://opensourcedit.com/sso\" target=\"_blank\">link</a>. </b> <br><br>Thank you! </p></td></tr><tr> <td style=\"height:40px;\">&nbsp;</td></tr></table> </td><tr> <td style=\"height:20px;\">&nbsp;</td></tr><tr> <td style=\"text-align:center;\"> <p style=\"font-size:14px; color:rgba(69, 80, 86, 0.7411764705882353); line-height:18px; margin:0 0 0;\"> &copy; softwright.in </p></td></tr><tr> <td style=\"height:80px;\">&nbsp;</td></tr></table> </td></tr></table></body></html>";
    }
}
