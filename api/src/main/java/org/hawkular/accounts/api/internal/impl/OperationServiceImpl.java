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

import org.hawkular.accounts.api.NamedOperation;
import org.hawkular.accounts.api.OperationService;
import org.hawkular.accounts.api.PermissionService;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Operation_;
import org.hawkular.accounts.api.model.Permission;
import org.hawkular.accounts.api.model.Role;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;

/**
 * Concrete implementation of {@link OperationService}.
 *
 * @see OperationService
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class OperationServiceImpl implements OperationService {
    @Inject
    @HawkularAccounts
    EntityManager em;

    @Inject
    PermissionService permissionService;

    @Inject
    RoleService roleService;

    @Override
    public Operation getByName(String name) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Operation> query = builder.createQuery(Operation.class);
        Root<Operation> root = query.from(Operation.class);
        query.select(root);
        query.where(builder.equal(root.get(Operation_.name), name));

        List<Operation> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            Operation operation = results.get(0);
            return operation;
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one operation found for name " + name);
        }

        return null;
    }

    @Override
    @Produces @NamedOperation
    public Operation produceOperationByName(InjectionPoint injectionPoint) {
        NamedOperation namedOperation = injectionPoint.getAnnotated().getAnnotation(NamedOperation.class);
        String operationName = namedOperation.value();
        return getByName(operationName);
    }


    @Override
    public Setup setup(Operation operation) {
        return new Setup(operation);
    }

    @Override
    public Setup setup(String operationName) {
        Operation operation = getByName(operationName);
        if (null == operation) {
            operation = new Operation(operationName);
            em.persist(operation);
        }
        return setup(operation);
    }

    public class Setup implements OperationService.Setup {

        private Operation operation;
        private Set<Role> roles;
        // this is a basic naive state tracking: ideally, we would compare the two lists, *but*,
        // what to do if the persona asks for a clear? isn't that an explicit instruction to remove everything that is
        // on the database and add again? even if that's so, is it that bad that we wouldn't do it, if the results
        // are the same? while we don't have a good answer, we keep it this way
        private boolean rolesHaveChanged = false;

        private Setup(Operation operation) {
            this.operation = operation;
            this.roles = permissionService.getPermittedRoles(operation);
        }

        @Override
        public OperationService.Setup add(Role role) {
            if (null == role) {
                throw new IllegalArgumentException("The given role is invalid (null).");
            }
            if (!roles.contains(role)) {
                Set<Role> implicitRoles = roleService.getImplicitPermittedRoles(role);
                rolesHaveChanged = true;
                roles.addAll(implicitRoles);
                roles.add(role);
            }
            return this;
        }

        @Override
        public OperationService.Setup add(String roleName) {
            return add(roleService.getByName(roleName));
        }

        @Override
        public OperationService.Setup add(Role role1, Role role2) {
            return add(role1).add(role2);
        }

        @Override
        public OperationService.Setup add(Role role1, Role role2, Role role3) {
            return add(role1).add(role2).add(role3);
        }

        @Override
        public OperationService.Setup add(Role... roles) {
            for (Role role : roles) {
                add(role);
            }
            return this;
        }

        @Override
        public OperationService.Setup clear() {
            rolesHaveChanged = true;
            roles.clear();
            return this;
        }

        @Override
        public OperationService persist() {
            doPersist();
            return OperationServiceImpl.this;
        }

        @Override
        public Operation make() {
            doPersist();
            return operation;
        }

        private void doPersist() {
            if (rolesHaveChanged) {
                // here, we have two options: first is to do a bulk delete, based on the operation
                // something like: DELETE FROM Permission p where p.operation = operation
                // it would be faster than go one by one, *but*, doing the way we are doing
                // means that we are leaving the permission on the 1st level cache (which might or might not be
                // useful at this point).
                // if this proves to be an issue, then we change it to bulk remove
                Set<Permission> permissions = permissionService.getPermissionsForOperation(operation);
                permissions.forEach(em::remove);
                roles.forEach(role -> em.persist(new Permission(operation, role)));
            }
        }
    }
}
