/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.mail.internet.InternetAddress;

import org.hawkular.accounts.api.InvitationService;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationJoinRequest;
import org.hawkular.accounts.backend.entity.OrganizationJoinRequestEvent;
import org.hawkular.commons.email.EmailDispatcher;

/**
 * Dispatches email notifications to the involved parties on a Join Request. When a new join request is made, the owner
 * of the organization is notified, as well as the user who applied for it. Once a decision is made (accept/reject),
 * the user who applied for it is also notified.
 *
 * @author Juraci Paixão Kröhling
 */
@PermitAll
@Singleton
public class JoinRequestNotificationDispatcher {
    public static final String HAWKULAR_BASE_URL = "HAWKULAR_BASE_URL";
    public static final String DEFAULT_HAWKULAR_BASE_URL = System.getenv(HAWKULAR_BASE_URL) == null ?
            "http://localhost:8080/" : System.getenv(HAWKULAR_BASE_URL);

    MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    EmailDispatcher emailDispatcher;

    @Inject
    InvitationService invitationService;

    public void dispatchNotification(@Observes OrganizationJoinRequestEvent event) {
        OrganizationJoinRequest request = event.getRequest();

        Map<String, Object> properties = new HashMap<>(3);
        properties.put("requestor", request.getPersona().getName());
        properties.put("orgName", request.getOrganization().getName());
        properties.put("pendingRequestsUrl",
                DEFAULT_HAWKULAR_BASE_URL
                        + "/hawkular-ui/organization/"+
                        request.getOrganization().getIdAsUUID().toString()
                        +"/memberships");

        switch (request.getStatus()) {
            case PENDING:
                sendNotificationsForPending(request, properties);
                break;
            case REJECTED:
                sendNotificationsForRejected(request, properties);
                break;
            case ACCEPTED:
                sendNotificationsForAccepted(request, properties);
                break;
        }
    }

    private void sendNotificationsForAccepted(OrganizationJoinRequest request, Map<String, Object> properties) {
        if (!(request.getPersona() instanceof HawkularUser)) {
            logger.joinRequestToOrganization();
            return;
        }

        HawkularUser user = (HawkularUser) request.getPersona();
        String email = user.getEmail();
        if (null == email || email.isEmpty()) {
            logger.joinRequestUserEmailIsEmpty();
            return;
        }

        try {
            emailDispatcher.dispatch(new InternetAddress(email),
                    "[hawkular] - You have been accepted to join " + request.getOrganization().getName(),
                    "join_request_approved_plain.ftl",
                    "join_request_approved_html.ftl",
                    properties);

        } catch (Exception e) {
            logger.joinRequestFailToSendAcceptedNotification(request.getId(), e);
        }
    }

    private void sendNotificationsForRejected(OrganizationJoinRequest request, Map<String, Object> properties) {
        if (!(request.getPersona() instanceof HawkularUser)) {
            logger.joinRequestToOrganization();
            return;
        }

        HawkularUser user = (HawkularUser) request.getPersona();
        String email = user.getEmail();
        if (null == email || email.isEmpty()) {
            logger.joinRequestUserEmailIsEmpty();
            return;
        }

        try {
            emailDispatcher.dispatch(new InternetAddress(email),
                    "[hawkular] - Join request declined",
                    "join_request_rejected_plain.ftl",
                    "join_request_rejected_html.ftl",
                    properties);

        } catch (Exception e) {
            logger.joinRequestFailToSendRejectedNotification(request.getId(), e);
        }
    }

    private void sendNotificationsForPending(OrganizationJoinRequest request, Map<String, Object> properties) {
        if (!(request.getPersona() instanceof HawkularUser)) {
            logger.joinRequestToOrganization();
            return;
        }

        HawkularUser user = (HawkularUser) request.getPersona();
        String email = user.getEmail();
        HawkularUser owner = getOwnerForOrganization(request.getOrganization());
        String ownersEmail = owner.getEmail();

        try {
            if (null == ownersEmail || ownersEmail.isEmpty()) {
                logger.joinRequestOwnersEmailIsEmpty();
            } else {
                emailDispatcher.dispatch(new InternetAddress(ownersEmail),
                        "[hawkular] - Join request for " + request.getOrganization().getName(),
                        "join_request_to_admin_plain.ftl",
                        "join_request_to_admin_html.ftl",
                        properties);
            }

            if (null == email || email.isEmpty()) {
                logger.joinRequestUserEmailIsEmpty();
            } else {
                emailDispatcher.dispatch(new InternetAddress(email),
                        "[hawkular] - Join request for " + request.getOrganization().getName(),
                        "join_request_to_requestor_plain.ftl",
                        "join_request_to_requestor_html.ftl",
                        properties);
            }

        } catch (Exception e) {
            logger.joinRequestFailToSendNotification(request.getId(), e);
        }
    }

    private HawkularUser getOwnerForOrganization(Organization organization) {
        if (organization.getOwner() instanceof HawkularUser) {
            return (HawkularUser) organization.getOwner();
        } else {
            return getOwnerForOrganization((Organization) organization.getOwner());
        }
    }

}
