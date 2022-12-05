package com.arcxp.platform.sdk.email.sendgrid;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.arcxp.platform.sdk.email.EmailModel;
import com.arcxp.platform.sdk.email.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Client;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Email service implementation for Send Grid sdk.
 */

@Component
public class SendGridEmailService implements EmailService {


    private static final Logger LOG = LoggerFactory.getLogger(SendGridEmailService.class);

    private final SendGrid sendGridClient;

    private final ObjectMapper objectMapper;

    private final Environment env;

    @Autowired
    public SendGridEmailService(Environment env, ObjectMapper objectMapper) {
        this.env = env;
        this.objectMapper = objectMapper;


        Client sendGridHttpClient = null;
        if (Boolean.parseBoolean(env.getProperty("tracing"))) {

            sendGridHttpClient = new Client(com.amazonaws.xray.proxies.apache.http.HttpClientBuilder.create().build());
        } else {
            sendGridHttpClient = new Client();
        }

        this.sendGridClient = new SendGrid(env.getProperty("email.sendgrid.apiKey"), sendGridHttpClient);

    }

    @Override
    public void sendTemplatedEmail(List<String> toAddresses, EmailModel model) throws IOException {
        sendTemplatedEmail(toAddresses, Collections.EMPTY_LIST, model);
    }

    @Override
    public void sendTemplatedEmail(List<String> toAddresses, List<String> bccAddresses, EmailModel model)
        throws IOException {
        Email from = new Email(env.getProperty("email.sendgrid.senderEmail"));

        Mail mail = new Mail();
        mail.setFrom(from);

        mail.setTemplateId(env.getProperty("email.sendgrid.emailId." + model.getEmailId() + ".templateId"));

        Personalization personalization = new Personalization();
        toAddresses.stream().forEach(address -> personalization.addTo(new Email(address)));
        bccAddresses.stream().forEach(address -> personalization.addBcc(new Email(address)));
        Map<String, Object> objectMap = objectMapper.convertValue(model, Map.class);

        objectMap.entrySet().stream().forEach((entry) ->
            personalization.addDynamicTemplateData(entry.getKey(), entry.getValue()));
        mail.addPersonalization(personalization);

        LOG.debug("Mail: " + objectMapper.writeValueAsString(mail));
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = this.sendGridClient.api(request);
            LOG.debug("SendGrid Status Code: {}", response.getStatusCode());
            LOG.debug("SendGrid Body: {}", response.getBody());
            LOG.debug("SendGrid Headers: {}", response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }

    }

}
