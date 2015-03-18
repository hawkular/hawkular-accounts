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
package org.hawkular.accounts.api.internal.impl;

import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.internal.adapter.NamedRole;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Resource_;
import org.hawkular.accounts.api.model.Role;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Main implementation of the {@link org.hawkular.accounts.api.ResourceService}. Consumers should get an instance of
 * this via CDI. This class should not be directly instantiated by the consumers.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class ResourceServiceImpl implements ResourceService {
    @Inject @HawkularAccounts
    EntityManager em;

    @Inject
    HawkularUser user;

    @Inject
    @NamedRole("Super User")
    Role superUser;

    @Override
    public Resource get(String id) {
        if (null == id) {
            throw new IllegalArgumentException("The given resource ID is invalid (null).");
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Resource> query = builder.createQuery(Resource.class);
        Root<Resource> root = query.from(Resource.class);
        query.select(root);
        query.where(builder.equal(root.get(Resource_.id), id));

        List<Resource> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            return results.get(0);
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one resource found for ID " + id);
        }

        return null;
    }

    @Override
    public Resource create(String id, Persona persona) {
        if (null == persona) {
            throw new IllegalArgumentException("The specified persona is invalid (null).");
        }
        return create(id, null, persona);
    }

    @Override
    public Resource create(String id, Resource parent) {
        if (null == parent) {
            throw new IllegalArgumentException("The given parent resource is invalid (null).");
        }
        return create(id, parent, null);
    }

    @Override
    public Resource create(String id, Resource parent, Persona persona) {
        if (null == parent && null == persona) {
            throw new IllegalArgumentException("Either parent or persona should be provided when creating a resource");
        }

        Resource resource = new Resource(id, persona, parent);
        em.persist(resource);

        if (persona != null) {
            PersonaResourceRole resourceRole = new PersonaResourceRole(persona, superUser, resource);
            em.persist(resourceRole);
        }

        return resource;
    }

    @Override
    public void delete(String id) {
        if (null == id) {
            throw new IllegalArgumentException("The given resource ID is invalid (null).");
        }

        Resource resource = get(id);
        if (resource != null) {
            em.remove(id);
        }
    }
}
