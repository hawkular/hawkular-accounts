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
package org.hawkular.accounts.backend.boundary;

import org.hawkular.accounts.backend.entity.Owner;
import org.hawkular.accounts.backend.entity.Resource;
import org.hawkular.accounts.backend.entity.Resource_;
import org.keycloak.KeycloakPrincipal;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
@Stateless
public class ResourceService {
    @Inject
    EntityManager em;

    @Inject
    UserService userService;

    /**
     * Retrieves a {@link Resource} based on its ID.
     *
     * @param id the resource's ID
     * @return the existing {@link Resource} or null if the resource doesn't exists.
     */
    public Resource getById(String id) {
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

    /**
     * Retrieves a {@link Resource} based on its ID or creates a new {@link Resource} if it doesn't exists.
     *
     * @param id    the resource's ID
     * @param owner if the resource doesn't exists, a new one is created with the specified owner
     * @return the existing {@link Resource} or a new one if it doesn't exists yet.
     */
    public Resource getOrCreateById(String id, Owner owner) {
        Resource resource = getById(id);
        if (null == resource) {
            resource = new Resource(id, owner);
            em.persist(resource);
        }

        return resource;
    }

    /**
     * Retrieves a {@link Resource} based on its ID or creates a new {@link Resource} if it doesn't exists.
     *
     * @param id        the resource's ID
     * @param principal if the resource doesn't exists, a new one is created with the specified principal
     * @return the existing {@link Resource} or a new one if it doesn't exists yet.
     * @see ResourceService#getOrCreateById(java.lang.String, org.hawkular.accounts.backend.entity.Owner)
     */
    public Resource getOrCreateById(String id, KeycloakPrincipal principal) {
        return getOrCreateById(id, userService.getByPrincipal(principal));
    }
}
