package com.arcxp.platform.sdk.email;

/**
 * Interface for Models to be used to populate Email Templates.
 */

public interface EmailModel {

    /**
     * The ID associating the Model to the Template in the email service.
     *
     * @return The Email Id
     */
    String getEmailId();

}
