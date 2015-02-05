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
package org.hawkular.accounts.backend.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an non-user entity that can own resources. It has itself an owner.
 *
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
@Entity
public class Organization extends Owner {

    @ManyToOne
    private Owner owner;

    @ManyToMany
    private final List<Owner> members = new ArrayList<>();

    protected Organization() { // jpa happy
        super();
    }

    public Organization(String id, Owner owner) {
        super(id);
        this.owner = owner;
    }

    public Owner getOwner() {
        return owner;
    }

    public List<Owner> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(Owner member) {
        if (members.contains(member)) {
            return;
        }

        members.add(member);
    }

    public void removeMember(Owner member) {
        if (!members.contains(member)) {
            return;
        }

        members.remove(member);
    }
}
