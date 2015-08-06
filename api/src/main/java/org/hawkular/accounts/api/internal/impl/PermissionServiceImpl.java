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

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hawkular.accounts.api.PermissionService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Permission;
import org.hawkular.accounts.api.model.Permission_;
import org.hawkular.accounts.api.model.Role;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class PermissionServiceImpl implements PermissionService {
    @Inject
    @HawkularAccounts
    EntityManager em;

    @Override
    public Set<Role> getPermittedRoles(Operation operation) {
        return getPermissionsForOperation(operation)
                .stream()
                .map(Permission::getRole)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Permission> getPermissionsForOperation(Operation operation) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Permission> query = builder.createQuery(Permission.class);
        Root<Permission> root = query.from(Permission.class);
        query.select(root);
        query.where(builder.equal(root.get(Permission_.operation), operation));

        return em.createQuery(query)
                .getResultList()
                .stream()
                .collect(Collectors.toSet());
    }
}
