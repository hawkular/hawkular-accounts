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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.hawkular.accounts.api.internal.ApplicationResources;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.model.Role;
import org.hawkular.accounts.common.ZonedDateTimeAdapter;
import org.junit.Before;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

/**
 * @author Juraci Paixão Kröhling
 */
public abstract class SessionEnabledTest {
    static boolean prepared;
    static Session session;

    RoleServiceImpl roleService = new RoleServiceImpl();
    UserServiceImpl userService = new UserServiceImpl();
    OperationServiceImpl operationService = new OperationServiceImpl();
    PermissionServiceImpl permissionService = new PermissionServiceImpl();
    OrganizationServiceImpl organizationService = new OrganizationServiceImpl();
    InvitationServiceImpl invitationService = new InvitationServiceImpl();
    OrganizationMembershipServiceImpl membershipService = new OrganizationMembershipServiceImpl();
    ResourceServiceImpl resourceService = new ResourceServiceImpl();
    PersonaResourceRoleServiceImpl personaResourceRoleService = new PersonaResourceRoleServiceImpl();
    PersonaServiceImpl personaService = new PersonaServiceImpl();
    PermissionCheckerImpl permissionChecker = new PermissionCheckerImpl();
    UserSettingsServiceImpl settingsService = new UserSettingsServiceImpl();
    Role superUser;
    Role administrator;
    Role auditor;
    Role deployer;
    Role maintainer;
    Role operator;
    Role monitor;

    @Before
    public void prepare() throws IOException, TTransportException, InterruptedException {
        prepareCassandra();
        ApplicationResources resources = new ApplicationResources();
        resources.setSession(session);

        ZonedDateTimeAdapter zonedDateTimeAdapter = new ZonedDateTimeAdapter();

        roleService.session = session;
        roleService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        roleService.createStatement = resources.getBoundStatement(BoundStatements.ROLES_CREATE);
        roleService.getByIdStatement = resources.getBoundStatement(BoundStatements.ROLES_GET_BY_ID);
        roleService.getByNameStatement = resources.getBoundStatement(BoundStatements.ROLES_GET_BY_NAME);

        userService.session = session;
        userService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        userService.createStatement = resources.getBoundStatement(BoundStatements.USER_CREATE);
        userService.allUsersStatement = resources.getBoundStatement(BoundStatements.USER_ALL);
        userService.getByIdStatement = resources.getBoundStatement(BoundStatements.USER_GET_BY_ID);
        userService.updateStatement = resources.getBoundStatement(BoundStatements.USER_UPDATE);

        permissionService.session = session;
        permissionService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        permissionService.operationService = operationService;
        permissionService.roleService = roleService;
        permissionService.getById = resources.getBoundStatement(BoundStatements.PERMISSION_GET_BY_ID);
        permissionService.getByOperation = resources.getBoundStatement(BoundStatements.PERMISSIONS_GET_BY_OPERATION);
        permissionService.createStatement = resources.getBoundStatement(BoundStatements.PERMISSION_CREATE);
        permissionService.deleteStatement = resources.getBoundStatement(BoundStatements.PERMISSION_DELETE);

        operationService.session = session;
        operationService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        operationService.getByName = resources.getBoundStatement(BoundStatements.OPERATION_GET_BY_NAME);
        operationService.getById = resources.getBoundStatement(BoundStatements.OPERATION_GET_BY_ID);
        operationService.createStatement = resources.getBoundStatement(BoundStatements.OPERATION_CREATE);
        operationService.roleService = roleService;
        operationService.permissionService = permissionService;

        resourceService.session = session;
        resourceService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        resourceService.personaResourceRoleService = personaResourceRoleService;
        resourceService.personaService = personaService;
        resourceService.getById = resources.getBoundStatement(BoundStatements.RESOURCE_GET_BY_ID);
        resourceService.getByPersona = resources.getBoundStatement(BoundStatements.RESOURCE_GET_BY_PERSONA);
        resourceService.createStatement = resources.getBoundStatement(BoundStatements.RESOURCE_CREATE);
        resourceService.transferStatement = resources.getBoundStatement(BoundStatements.RESOURCE_TRANSFER);

        personaResourceRoleService.session = session;
        personaResourceRoleService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        personaResourceRoleService.resourceService= resourceService;
        personaResourceRoleService.personaService = personaService;
        personaResourceRoleService.roleService = roleService;
        personaResourceRoleService.createStatement = resources.getBoundStatement(BoundStatements.PRR_CREATE);
        personaResourceRoleService.getById = resources.getBoundStatement(BoundStatements.PRR_GET_BY_ID);
        personaResourceRoleService.getByPersona = resources.getBoundStatement(BoundStatements.PRR_GET_BY_PERSONA);
        personaResourceRoleService.getByResource = resources.getBoundStatement(BoundStatements.PRR_GET_BY_RESOURCE);
        personaResourceRoleService.removeStatement = resources.getBoundStatement(BoundStatements.PRR_REMOVE);

        membershipService.session = session;
        membershipService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        membershipService.resourceService= resourceService;
        membershipService.personaService = personaService;
        membershipService.organizationService = organizationService;
        membershipService.roleService = roleService;
        membershipService.getById = resources.getBoundStatement(BoundStatements.MEMBERSHIP_GET_BY_ID);
        membershipService.getByPersona = resources.getBoundStatement(BoundStatements.MEMBERSHIP_GET_BY_PERSONA);
        membershipService.removeStatement = resources.getBoundStatement(BoundStatements.MEMBERSHIP_REMOVE);
        membershipService.createStatement = resources.getBoundStatement(BoundStatements.MEMBERSHIP_CREATE);
        membershipService.changeRoleStatement = resources.getBoundStatement(BoundStatements.MEMBERSHIP_CHANGE_ROLE);
        membershipService.getByOrganization =
                resources.getBoundStatement(BoundStatements.MEMBERSHIP_GET_BY_ORGANIZATION);

        organizationService.session = session;
        organizationService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        organizationService.membershipService = membershipService;
        organizationService.resourceService = resourceService;
        organizationService.invitationService = invitationService;
        organizationService.personaService = personaService;
        organizationService.createStatement = resources.getBoundStatement(BoundStatements.ORGANIZATION_CREATE);
        organizationService.getById = resources.getBoundStatement(BoundStatements.ORGANIZATION_GET_BY_ID);
        organizationService.getByName = resources.getBoundStatement(BoundStatements.ORGANIZATION_GET_BY_NAME);
        organizationService.getByOwner = resources.getBoundStatement(BoundStatements.ORGANIZATION_GET_BY_OWNER);
        organizationService.transferStatement = resources.getBoundStatement(BoundStatements.ORGANIZATION_TRANSFER);
        organizationService.removeStatement = resources.getBoundStatement(BoundStatements.ORGANIZATION_REMOVE);

        invitationService.session = session;
        invitationService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        invitationService.roleService = roleService;
        invitationService.organizationService = organizationService;
        invitationService.userService = userService;
        invitationService.membershipService = membershipService;
        invitationService.getByTokenStatement = resources.getBoundStatement(BoundStatements.INVITATION_GET_BY_TOKEN);
        invitationService.createStatement = resources.getBoundStatement(BoundStatements.INVITATIONS_CREATE);
        invitationService.acceptStatement = resources.getBoundStatement(BoundStatements.INVITATIONS_ACCEPT);
        invitationService.deleteStatement = resources.getBoundStatement(BoundStatements.INVITATIONS_DELETE);
        invitationService.dispatchedStatement = resources.getBoundStatement(BoundStatements.INVITATIONS_DISPATCH);
        invitationService.getByOrganizationStatement = resources.getBoundStatement(
                BoundStatements.INVITATIONS_GET_BY_ORGANIZATION
        );

        settingsService.session = session;
        settingsService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        settingsService.userService = userService;
        settingsService.getById = resources.getBoundStatement(BoundStatements.SETTINGS_GET_BY_ID);
        settingsService.getByUser = resources.getBoundStatement(BoundStatements.SETTINGS_GET_BY_USER);
        settingsService.createStatement = resources.getBoundStatement(BoundStatements.SETTINGS_CREATE);
        settingsService.updateStatement = resources.getBoundStatement(BoundStatements.SETTINGS_UPDATE);

        personaService.membershipService = membershipService;
        personaService.organizationService = organizationService;
        personaService.userService = userService;
        personaService.resourceService = resourceService;
        personaService.roleService = roleService;
        personaService.personaResourceRoleService = personaResourceRoleService;

        permissionChecker.permissionService = permissionService;
        permissionChecker.personaService = personaService;
        permissionChecker.resourceService = resourceService;

        superUser = roleService.getOrCreateByName("SuperUser", "");
        administrator = roleService.getOrCreateByName("Administrator", "");
        auditor = roleService.getOrCreateByName("Auditor", "");
        deployer = roleService.getOrCreateByName("Deployer", "");
        maintainer = roleService.getOrCreateByName("Maintainer", "");
        operator = roleService.getOrCreateByName("Operator", "");
        monitor = roleService.getOrCreateByName("Monitor", "");

        resourceService.superUser = superUser;
        organizationService.superUser = superUser;
    }

    private void prepareCassandra() throws IOException, TTransportException, InterruptedException {
        if (prepared) {
            return;
        }

        startServerIfNotRunning();
        cleanDatabase();
        prepared = true;
    }

    private void startServerIfNotRunning() throws IOException, TTransportException, InterruptedException {
        try {
            session = new Cluster.Builder()
                    .addContactPoints("localhost")
                    .withProtocolVersion(ProtocolVersion.V3)
                    .build().connect();
        } catch (NoHostAvailableException e) {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
            session = new Cluster.Builder()
                    .addContactPoints("localhost")
                    .withPort(9142)
                    .withProtocolVersion(ProtocolVersion.V3)
                    .build().connect();
        }
    }

    public void cleanDatabase() throws IOException {
        session
                .getCluster().getMetadata().getKeyspaces()
                .stream()
                .filter(k -> k.getName().equals("hawkular_accounts"))
                .limit(1) // once we find it, no need to keep going through the stream
                .forEach(k -> session.execute("DROP KEYSPACE hawkular_accounts"));

        InputStream input = getClass().getResourceAsStream("/hawkular_accounts.cql");
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            String content = buffer.lines().collect(Collectors.joining("\n"));
            for (String cql : content.split("(?m)^-- #.*$")) {
                if (!cql.startsWith("--")) {
                    session.execute(cql);
                }
            }
        }
    }

}
