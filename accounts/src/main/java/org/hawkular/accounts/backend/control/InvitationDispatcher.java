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

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.mail.Session;
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
    MsgLogger logger = MsgLogger.LOGGER;

    @Inject @HawkularAccounts
    EntityManager em;

    @Resource
    Session mailSession;

    public void dispatchInvitation(@Observes InvitationCreatedEvent event) {
        Invitation invitation = event.getInvitation();
        if (null == invitation) {
            throw new IllegalArgumentException("Invitation event doesn't contain an invitation.");
        }

        invitation = em.merge(invitation);
        invitation.setDispatched();
        em.persist(invitation);
        logger.invitationSubmitted(invitation.getId(), invitation.getToken());
    }

}
