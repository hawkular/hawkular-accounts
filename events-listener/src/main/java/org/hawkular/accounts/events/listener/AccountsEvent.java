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
package org.hawkular.accounts.events.listener;

import org.hawkular.accounts.api.model.Persona;

/**
 * @author Juraci Paixão Kröhling
 */
public class AccountsEvent {
    private Persona persona;
    private String eventId;
    private String action;

    public AccountsEvent(Persona persona, String eventId, String action) {
        this.persona = persona;
        this.eventId = eventId;
        this.action = action;
    }

    public AccountsEvent() {
    }

    /**
     * The persona associated with this event. As this is an event coming from Keycloak, this is always a specific user.
     * @return the persona associated with this event.
     */
    public Persona getPersona() {
        return persona;
    }

    /**
     * A unique ID (UUID) for this event.
     * @return an UUID as string for this event.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * The Keycloak event.
     *
     * @return a String representation of one of the possible values from the enum org.keycloak.events.EventType
     */
    public String getAction() {
        return action;
    }
}
