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

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Represents an model that might own resources. Concrete implementations could be Organization or User.
 *
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Owner extends BaseEntity {
    protected Owner() { // JPA happy
    }

    public Owner(String id) {
        super(id);
    }
}
