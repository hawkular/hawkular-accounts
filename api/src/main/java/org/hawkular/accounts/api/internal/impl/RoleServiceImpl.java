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

import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.internal.adapter.NamedRole;
import org.hawkular.accounts.api.model.Role;
import org.hawkular.accounts.api.model.Role_;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class RoleServiceImpl implements RoleService {

    @Inject @HawkularAccounts
    EntityManager em;

    @Override
    public Role getByName(String name) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Role> query = builder.createQuery(Role.class);
        Root<Role> root = query.from(Role.class);
        query.select(root);
        query.where(builder.equal(root.get(Role_.name), name));

        List<Role> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            Role role = results.get(0);
            return role;
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one role found for name " + name);
        }

        return null;
    }

    @Override
    public Set<Role> getImplicitUserRoles(String name) {
        Set<Role> implicitRoles = new HashSet<>(7);
        switch (name) {
            case "Super User":
                implicitRoles.add(getByName("Auditor"));
                implicitRoles.add(getByName("Administrator"));
                implicitRoles.add(getByName("Deployer"));
            case "Deployer":
            case "Administrator":
                implicitRoles.add(getByName("Maintainer"));
            case "Maintainer":
                implicitRoles.add(getByName("Operator"));
            case "Auditor":
            case "Operator":
                implicitRoles.add(getByName("Monitor"));
            case "Monitor":
                break;
            default:
                throw new IllegalArgumentException("Unrecognized role: '" + name + "'");

        }
        return implicitRoles;
    }

    @Override
    public Set<Role> getImplicitPermittedRoles(String name) {
        Set<Role> implicitRoles = new HashSet<>(7);
        switch (name) {
            case "Monitor":
                implicitRoles.add(getByName("Operator"));
                implicitRoles.add(getByName("Auditor"));
            case "Operator":
                implicitRoles.add(getByName("Maintainer"));
            case "Maintainer":
                implicitRoles.add(getByName("Administrator"));
                implicitRoles.add(getByName("Deployer"));
            case "Deployer":
            case "Administrator":
            case "Auditor":
                implicitRoles.add(getByName("Super User"));
            case "Super User":
                break;
            default:
                throw new IllegalArgumentException("Unrecognized role: '" + name + "'");

        }
        return implicitRoles;
    }

    @Override
    public Set<Role> getImplicitUserRoles(Role role) {
        // I feel dirty for doing this, but in this case, we need the string more than the object itself...
        return getImplicitUserRoles(role.getName());
    }

    @Override
    public Set<Role> getImplicitPermittedRoles(Role role) {
        // I feel dirty for doing this, but in this case, we need the string more than the object itself...
        return getImplicitPermittedRoles(role.getName());
    }

    @Override
    @Produces @NamedRole
    public Role produceRoleByName(InjectionPoint injectionPoint) {
        NamedRole namedRole = injectionPoint.getAnnotated().getAnnotation(NamedRole.class);
        String roleName = namedRole.value();
        return getByName(roleName);
    }
}
