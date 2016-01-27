/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.hawkular.accounts.api.NamedRole;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.internal.NamedStatement;
import org.hawkular.accounts.api.model.Role;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class RoleServiceImpl extends BaseServiceImpl<Role> implements RoleService {
    MsgLogger logger = MsgLogger.LOGGER;

    @Inject @NamedStatement(BoundStatements.ROLES_GET_BY_ID)
    Instance<BoundStatement> stmtGetByIdInstance;

    @Inject @NamedStatement(BoundStatements.ROLES_CREATE)
    Instance<BoundStatement> stmtCreateInstance;

    @Inject @NamedStatement(BoundStatements.ROLES_GET_BY_NAME)
    Instance<BoundStatement> stmtGetByNameInstance;

    @Override
    public Role getById(UUID id) {
        return getById(id, stmtGetByIdInstance.get());
    }

    @Override
    public Role create(String name, String description) {
        BoundStatement stmtCreate = stmtCreateInstance.get();
        if (null != getByName(name)) {
            // we already have a role with this name...
            throw new InvalidParameterException("There's already a role with the given name.");
        }

        Role role = new Role(name, description);
        bindBasicParameters(role, stmtCreate);
        stmtCreate.setString("name", name);
        stmtCreate.setString("description", description);
        session.execute(stmtCreate);
        logger.roleCreated(name);
        return role;
    }

    @Override
    public Role getByName(String name) {
        return getSingleRecord(stmtGetByNameInstance.get().setString("name", name));
    }

    @Override
    public Set<Role> getImplicitUserRoles(String name) {
        Set<Role> implicitRoles = new HashSet<>(7);
        switch (name) {
            case "SuperUser":
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
                implicitRoles.add(getByName("SuperUser"));
            case "SuperUser":
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

    @Override
    public Role getOrCreateByName(String name, String description) {
        Role role = getByName(name);
        if (null == role) {
            role = create(name, description);
        }
        return role;
    }

    @Override
    Role getFromRow(Row row) {
        String name = row.getString("name");
        String description = row.getString("description");

        Role.Builder builder = new Role.Builder();
        super.mapBaseFields(row, builder);
        return builder.description(description).name(name).build();
    }
}
