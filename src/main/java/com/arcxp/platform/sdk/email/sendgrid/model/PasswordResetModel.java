package com.arcxp.platform.sdk.email.sendgrid.model;

import com.arcxp.platform.sdk.email.EmailModel;

/**
 * Password Reset email model.
 */
public class PasswordResetModel implements EmailModel {

    private final String resetPasswordLink;

    public PasswordResetModel(String resetPasswordLink) {
        this.resetPasswordLink = resetPasswordLink;
    }

    @Override
    public String getEmailId() {
        return "2";
    }

    public String getResetPasswordLink() {
        return resetPasswordLink;
    }
}
