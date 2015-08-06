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

package org.hawkular.accounts.events.listener.internal;

import javax.annotation.security.PermitAll;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.UserService;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.events.listener.AccountsEvent;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(
                propertyName = "destinationLookup",
                propertyValue = "topic/HawkularAccountsEvents"
        ),

        @ActivationConfigProperty(
                propertyName = "destination",
                propertyValue = "HawkularAccountsEvents"
        ),

        @ActivationConfigProperty(
                propertyName = "destinationType",
                propertyValue = "javax.jms.Topic"
        ),

        @ActivationConfigProperty(
                propertyName = "connectionFactoryJndiName",
                propertyValue = "java:/HawkularBusConnectionFactory"
        )
})
@PermitAll
public class AccountsMessageListener implements MessageListener {
    private final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    Event<AccountsEvent> event;

    @Inject
    PersonaService personaService;

    @Inject
    UserService userService;

    @Override
    public void onMessage(Message message) {
        logger.eventReceived();
        try {
            String action = message.getStringProperty("action");
            String eventId = message.getStringProperty("eventId");
            String userId = message.getStringProperty("userId");
            Persona persona = personaService.get(userId);

            if (null == persona) {
                // it's probably a new user
                persona = userService.getOrCreateById(userId);
            }

            AccountsEvent accountsEvent = new AccountsEvent(persona, eventId, action);
            event.fire(accountsEvent);
            logger.eventProcessed(action, eventId, userId);
        } catch (JMSException e) {
            logger.errorProcessingEvent(e);
        }
    }
}
