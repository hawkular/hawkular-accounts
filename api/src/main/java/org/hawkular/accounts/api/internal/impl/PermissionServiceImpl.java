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
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

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
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class PermissionServiceImpl extends BaseServiceImpl<Permission> implements PermissionService {
    @Inject
    OperationService operationService;

    @Inject
    RoleService roleService;

    @Inject @NamedStatement(BoundStatements.PERMISSION_GET_BY_ID)
    BoundStatement getById;

    @Inject @NamedStatement(BoundStatements.PERMISSION_DELETE)
    BoundStatement deleteStatement;

    @Inject @NamedStatement(BoundStatements.PERMISSIONS_GET_BY_OPERATION)
    BoundStatement getByOperation;

    @Inject @NamedStatement(BoundStatements.PERMISSION_CREATE)
    BoundStatement createStatement;

    @Override
    public Set<Role> getPermittedRoles(Operation operation) {
        return getPermissionsForOperation(operation)
                .stream()
                .map(Permission::getRole)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Permission> getPermissionsForOperation(Operation operation) {
        getByOperation.setUUID("operation", operation.getIdAsUUID());
        return getList(getByOperation).stream().collect(Collectors.toSet());
    }

    @Override
    public Permission getById(UUID id) {
        return getById(id, getById);
    }

    @Override
    public Permission create(Operation operation, Role role) {
        Permission permission = new Permission(operation, role);

        bindBasicParameters(permission, createStatement);
        createStatement.setUUID("operation", permission.getOperation().getIdAsUUID());
        createStatement.setUUID("role", permission.getRole().getIdAsUUID());

        session.execute(createStatement);
        return permission;
    }

    @Override
    public void remove(Permission permission) {
        deleteStatement.setUUID("id", permission.getIdAsUUID());
        session.execute(deleteStatement);
    }

    @Override
    Permission getFromRow(Row row) {
        Operation operation = operationService.getById(row.getUUID("operation"));
        Role role = roleService.getById(row.getUUID("role"));

        Permission.Builder builder = new Permission.Builder();
        super.mapBaseFields(row, builder);
        return builder.operation(operation).role(role).build();
    }
}
