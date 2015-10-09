/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.accounts.backend.control;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;

import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.backend.entity.InvitationCreatedEvent;

/**
 * @author Juraci Paixão Kröhling
 */
@PermitAll
@Singleton
public class InvitationDispatcher {
    public static final String HAWKULAR_BASE_URL = "HAWKULAR_BASE_URL";
    public static final String DEFAULT_HAWKULAR_BASE_URL = System.getenv(HAWKULAR_BASE_URL) == null ?
            "http://localhost:8080/" : System.getenv(HAWKULAR_BASE_URL);

    MsgLogger logger = MsgLogger.LOGGER;

    @Inject @HawkularAccounts
    EntityManager em;

    @Resource(lookup = "java:jboss/mail/Default")
    Session mailSession;

    Map<String, String> defaultProperties = new HashMap<>();

    public void dispatchInvitation(@Observes InvitationCreatedEvent event) {
        Invitation invitation = event.getInvitation();
        if (null == invitation) {
            throw new IllegalArgumentException("Invitation event doesn't contain an invitation.");
        }

        invitation = em.merge(invitation);

        Message message = new MimeMessage(mailSession);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("You have been invited to join the organization ");
            sb.append(invitation.getOrganization().getName()).append(".\r\n");
            sb.append("\r\n");
            sb.append("To accept this invitation, simply click on the link below.\r\n");
            sb.append("If you don't have an account yet, don't worry: just click on the link below and ");
            sb.append("register for a new account.\r\n");
            sb.append("\r\n");
            sb.append(DEFAULT_HAWKULAR_BASE_URL);
            sb.append("hawkular-ui/invitation/accept/");
            sb.append(invitation.getToken());
            sb.append("\r\n");
            sb.append("\r\n");
            sb.append("This invitation was submitted to you by ");
            sb.append(invitation.getInvitedBy().getName());
            sb.append("\r\n");

            message.setFrom(new InternetAddress("noreply@hawkular.org", "Hawkular"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(invitation.getEmail()));
            message.setSubject("[hawkular] - You have been invited to join an organization.");
            message.setContent(sb.toString(), "text/plain");
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.invitationExceptionPreparingMessage(invitation.getId(), e.getMessage());
            return;
        }

        try {
            Transport.send(message);
        } catch (MessagingException e) {
            logger.invitationExceptionSendingMessage(invitation.getId(), e.getMessage());
            return;
        }

        // if we reached this point, everything went fine!
        invitation.setDispatched();
        em.persist(invitation);
        logger.invitationSubmitted(invitation.getId(), invitation.getToken());
    }

}
