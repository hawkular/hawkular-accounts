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
package org.hawkular.accounts.keycloak.events;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * @author Juraci Paixão Kröhling
 */
public class HawkularRESTEventListenerProviderFactory implements EventListenerProviderFactory {
    private final Set<EventType> excludedEvents = new HashSet<>();

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new HawkularRESTEventListenerProvider(Collections.unmodifiableSet(excludedEvents));
    }

    @Override
    public void init(Config.Scope config) {
        String[] excludes = config.getArray("excludes");
        if (excludes != null) {
            for (String e : excludes) {
                excludedEvents.add(EventType.valueOf(e));
            }
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "hawkular-rest";
    }
}
