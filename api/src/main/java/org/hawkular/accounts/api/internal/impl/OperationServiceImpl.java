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

import java.security.InvalidParameterException;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.hawkular.accounts.api.NamedOperation;
import org.hawkular.accounts.api.OperationService;
import org.hawkular.accounts.api.PermissionService;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.internal.NamedStatement;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Permission;
import org.hawkular.accounts.api.model.Role;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Concrete implementation of {@link OperationService}.
 *
 * @see OperationService
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class OperationServiceImpl extends BaseServiceImpl<Operation> implements OperationService {
    @Inject
    PermissionService permissionService;

    @Inject
    RoleService roleService;

    @Inject @NamedStatement(BoundStatements.OPERATION_GET_BY_NAME)
    Instance<BoundStatement> stmtGetByNameInstance;

    @Inject @NamedStatement(BoundStatements.OPERATION_GET_BY_ID)
    Instance<BoundStatement> stmtGetByIdInstance;

    @Inject @NamedStatement(BoundStatements.OPERATION_CREATE)
    Instance<BoundStatement> stmtCreateInstance;

    @Override
    public Operation getByName(String name) {
        BoundStatement stmtGetByName = this.stmtGetByNameInstance.get();
        stmtGetByName.setString("name", name);
        return getSingleRecord(stmtGetByName);
    }

    @Override
    public Operation getById(UUID id) {
        return getById(id, stmtGetByIdInstance.get());
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
            operation = create(operationName);
        }
        return setup(operation);
    }

    @Override
    Operation getFromRow(Row row) {
        Operation.Builder builder = new Operation.Builder();
        mapBaseFields(row, builder);
        return builder.name(row.getString("name")).build();
    }

    /**
     * This is not part of the public API. We want people to create Operations based on the setup(), but we might
     * want not want to use setup() for unit tests.
     * @param name    the operation name to create
     * @return the newly created Operation.
     * @throws InvalidParameterException if an operation with the given name already exists.
     */
    Operation create(String name) {
        BoundStatement stmtCreate = stmtCreateInstance.get();
        if (null != getByName(name)) {
            // we already have a role with this name...
            throw new InvalidParameterException("There's already an operation with the given name.");
        }

        Operation operation = new Operation(name);
        bindBasicParameters(operation, stmtCreate);
        stmtCreate.setString("name", name);

        session.execute(stmtCreate);
        return operation;
    }

    /**
     * Similar to {@link #create(String)}, but returns an existing Operation should one exist.
     * @param name    the operation name to create
     * @return the newly created Operation if none exists, or the existing one.
     * @see #create(String)
     */
    Operation getOrCreateByName(String name) {
        Operation byName = getByName(name);
        if (null != byName) {
            return byName;
        }
        return create(name);
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
                // for now, the simple thing: one by one delete... if it's too problematic, bulk remove them!
                Set<Permission> permissions = permissionService.getPermissionsForOperation(operation);
                permissions.forEach(permissionService::remove);
                roles.forEach(role -> permissionService.create(operation, role));
            }
        }
    }
}
