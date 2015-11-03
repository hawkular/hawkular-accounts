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
 * Represents an object that might be a member of an organization.
 *
 * @author Juraci Paixão Kröhling
 */
public class Member extends BaseEntity {
    public Member(String id) {
        super(id);
    }

    public Member(UUID id) {
        super(id);
    }

    public Member(UUID id, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        super(id, createdAt, updatedAt);
    }
}
