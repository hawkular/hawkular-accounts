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
 * Stores the role for a given user on a specific Resource. For instance, user jdoe is "Super User" on resource "node1"
 * An user might have a different role for different resources in the same tree. For instance, "node1" is the parent
 * of "database1", and the user "jdoe" above is only a "Monitor" of this sub resource. An user is allowed to have
 * one role or more roles for a given resource. In other words: an user can be the Auditor and the Monitor of the
 * same resource.
 *
 * @author Juraci Paixão Kröhling
 */
@Entity
public class PersonaResourceRole extends BaseEntity {
    @ManyToOne
    private Persona persona;

    @ManyToOne
    private Role role;

    @ManyToOne
    private Resource resource;

    protected PersonaResourceRole() { // JPA happy
    }

    public PersonaResourceRole(Persona persona, Role role, Resource resource) {
        this.persona = persona;
        this.role = role;
        this.resource = resource;
    }

    public PersonaResourceRole(String id, Persona persona, Role role, Resource resource) {
        super(id);
        this.persona = persona;
        this.role = role;
        this.resource = resource;
    }

    public Persona getPersona() {
        return persona;
    }

    public Role getRole() {
        return role;
    }

    public Resource getResource() {
        return resource;
    }
}
