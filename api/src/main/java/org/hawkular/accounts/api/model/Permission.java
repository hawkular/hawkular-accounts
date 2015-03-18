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
import javax.persistence.ManyToOne;

/**
 * Represents the permissions for a given resource. For instance, a resource "node1" might allow the operation
 * "metric-create" for role "Super User".
 *
 * @author Juraci Paixão Kröhling
 */
@Entity
public class Permission extends BaseEntity {
    @ManyToOne
    private Operation operation;

    @ManyToOne
    private Role role;

    protected Permission() { // JPA happy
    }

    public Permission(Operation operation, Role role) {
        this.operation = operation;
        this.role = role;
    }

    public Permission(String id, Operation operation, Role role) {
        super(id);
        this.operation = operation;
        this.role = role;
    }

    public Operation getOperation() {
        return operation;
    }

    public Role getRole() {
        return role;
    }
}
