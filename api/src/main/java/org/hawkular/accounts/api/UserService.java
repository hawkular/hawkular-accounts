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
package org.hawkular.accounts.api;

import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.HawkularUser_;
import org.keycloak.KeycloakPrincipal;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
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
@PermitAll
public class UserService {

    @Inject @HawkularAccounts
    EntityManager em;

    @Resource
    SessionContext sessionContext;

    /**
     * Retrieves the current user for the request.
     *
     * @return the current user
     */
    @Produces
    public HawkularUser getCurrent() {
        return getOrCreateById(sessionContext.getCallerPrincipal().getName());
    }

    /**
     * Retrieves an {@link HawkularUser} based in its ID.
     *
     * @param id the user ID
     * @return the existing user with the given ID or null if the user is not found.
     */
    public HawkularUser getById(String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<HawkularUser> query = builder.createQuery(HawkularUser.class);
        Root<HawkularUser> root = query.from(HawkularUser.class);
        query.select(root);
        query.where(builder.equal(root.get(HawkularUser_.id), id));

        List<HawkularUser> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            return results.get(0);
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one user found for ID " + id);
        }

        return null;
    }

    /**
     * Retrieves an {@link HawkularUser} based on the {@link KeycloakPrincipal}.
     *
     * @param principal the {@link KeycloakPrincipal}
     * @return an {@link HawkularUser} instance representing the user for the {@link KeycloakPrincipal}.It's never null.
     */
    public HawkularUser getByPrincipal(KeycloakPrincipal principal) {
        return getOrCreateById(principal.getName());
    }

    /**
     * Retrieves an {@link HawkularUser} based on its ID. If no user is found, a new one is created and returned.
     *
     * @param id the user ID
     * @return an {@link HawkularUser} instance representing the user with the given ID. It's never null.
     */
    public HawkularUser getOrCreateById(String id) {
        HawkularUser user = getById(id);
        if (null == user) {
            user = new HawkularUser(id);
            em.persist(user);
        }

        return user;
    }
}
