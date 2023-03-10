package com.softwright.iam.models;

import jakarta.validation.constraints.NotBlank;

public class MailRequest {
    @NotBlank
    public String to;

    @NotBlank
    public String from;

    @NotBlank
    public String subject;

    @NotBlank
    public String html;

    @NotBlank
    public String text;

    public MailRequest(String to, String from, String subject, String html, String text) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.html = html;
        this.text = text;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
