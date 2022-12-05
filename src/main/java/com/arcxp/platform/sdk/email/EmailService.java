package com.arcxp.platform.sdk.email;

import java.io.IOException;
import java.util.List;

/**
 * Service for sending emails.
 */

public interface EmailService {

    /**
     * Send a dynamic email based on a model and template.
     *
     * @param toAddresses List of addresses to send in Email's To field.
     * @param model       The model to be used to populate the email.
     * @throws IOException Exception thrown on sending email.
     */
    void sendTemplatedEmail(List<String> toAddresses, EmailModel model) throws IOException;

    /**
     * Send a dynamic email based on a model and template with BCC.
     *
     * @param toAddresses  List of addresses to send in Email's To field.
     * @param bccAddresses List of addresses to send in Email's Blind Copy field.
     * @param model        The model to be used to populate the email.
     * @throws IOException Exception thrown on sending email.
     */
    void sendTemplatedEmail(List<String> toAddresses, List<String> bccAddresses, EmailModel model)
        throws IOException;
}
