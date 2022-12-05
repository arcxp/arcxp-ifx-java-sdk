package com.arcxp.platform.sdk.email.sendgrid.model;

import com.arcxp.platform.sdk.email.EmailModel;

/**
 * Verify Email model.
 */
public class VerifyEmailModel implements EmailModel {

    private final String verifyLink;

    public VerifyEmailModel(String verifyLink) {
        this.verifyLink = verifyLink;
    }

    @Override
    public String getEmailId() {
        return "1";
    }

    public String getVerifyLink() {
        return verifyLink;
    }
}
