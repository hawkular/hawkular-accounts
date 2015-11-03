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
package org.hawkular.accounts.api.model;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents a persona that is able to login into the main application. Users might impersonate Organizations, so
 * that all actions are taken as if they were from the Organization itself.
 *
 * @author Juraci Paixão Kröhling
 */
public abstract class Persona extends Member {
    public Persona(String id) {
        super(id);
    }

    public Persona(UUID id) {
        super(id);
    }

    public abstract String getName();

    public Persona(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        super(id, createdAt, updatedAt);
    }
}
