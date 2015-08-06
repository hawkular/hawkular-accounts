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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Represents a resource that is meant to be protected. Each module is free to define their own rules for creating
 * Resources or defining its context. Examples of Resources could be: alerts, metrics, inventory items, ...
 *
 * Each resource is either owner by an {@link Persona} or is a sub resource (ie: it has a
 * parent Resource). At the end of the chain, there must be a valid owner.
 *
 * The parent of a Resource can be reset by first setting an owner and then setting the parent to null. Failure to
 * set an owner before resetting the parent will lead to {@link java.lang.IllegalStateException}. Similarly,
 * attempting to set the Owner to null without a valid parent first will lead to {@link java.lang
 * .IllegalStateException}.
 *
 * @author Juraci Paixão Kröhling
 */
@Entity
public class Resource extends BaseEntity {

    @ManyToOne
    private Persona persona;

    /**
     * Represents the parent resource for this resource.
     */
    @ManyToOne
    private Resource parent = null;

    /**
     * Transient list of sub resources for this resource.
     */
    @OneToMany(mappedBy = "parent")
    private List<Resource> children = new ArrayList<>();

    protected Resource() { // JPA happy
    }

    /**
     * Creates a new resource with the given owner.
     * @param persona     the owner of this sub resource
     * @throws IllegalStateException if the owner is null
     */
    public Resource(Persona persona) {
        setPersona(persona);
    }

    /**
     * Creates a new sub resource with a parent. Ownership of this resource might be delegated to the parent's owner.
     * @param parent    the parent of this sub resource
     * @throws IllegalStateException if the parent is null
     */
    public Resource(Resource parent) {
        setParent(parent);
    }

    /**
     * Creates a new sub resource with a parent and an owner, which may or may not be the same as the parent's.
     * @param parent    the parent of this sub resource
     * @param persona     the owner of this sub resource
     * @throws IllegalStateException if both the parent and the owner are null
     */
    public Resource(Persona persona, Resource parent) {
        if (null == persona && null == parent) {
            throw new IllegalStateException("A resource should either have a valid parent or an owner.");
        }
        this.persona = persona;
        this.parent = parent;
    }

    /**
     * Creates a new resource with the given id and owner.
     * @param id        the id that this resource should have or null for a new UUID
     * @param parent    the parent of this sub resource
     * @throws IllegalStateException if the owner is null
     */
    public Resource(String id, Resource parent) {
        super(id);
        setParent(parent);
    }

    /**
     * Creates a new resource with the given id and owner.
     * @param id        the id that this resource should have or null for a new UUID
     * @param persona     the owner of this sub resource
     * @throws IllegalStateException if the owner is null
     */
    public Resource(String id, Persona persona) {
        super(id);
        setPersona(persona);
    }

    /**
     * Creates a new sub resource with the given id, owner and parent resource.
     * @param id        the id that this resource should have or null for a new UUID
     * @param parent    the parent of this sub resource
     * @param persona     the owner of this sub resource
     * @throws IllegalStateException if both the parent and the owner are null
     */
    public Resource(String id, Persona persona, Resource parent) {
        super(id);
        if (null == persona && null == parent) {
            throw new IllegalStateException("A resource should either have a valid parent or an owner.");
        }
        this.persona = persona;
        this.parent = parent;
    }

    public Persona getPersona() {
        return persona;
    }

    public Resource getParent() {
        return parent;
    }

    public List<Resource> getSubResources() {
        return Collections.unmodifiableList(this.children);
    }

    public void setParent(Resource parent) {
        if (null == parent && null == this.persona) {
            throw new IllegalStateException("A resource should either have a valid parent or an owner.");
        }

        if (this.parent != null) {
            // we are changing parents
            if (this.parent.children.contains(this)) {
                // old parent has a reference to this, let's remove it (the list is transient, so, don't worry about
                // persistence)
                this.parent.children.remove(this);
            }
        }
        this.parent = parent;

        if (parent != null) {
            this.parent.children.add(this);
        }
    }

    public void setPersona(Persona persona) {
        if (null == persona && null == this.parent) {
            throw new IllegalStateException("A resource should either have a valid parent or an owner.");
        }
        this.persona = persona;
    }
}
