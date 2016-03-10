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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;

import org.apache.thrift.transport.TTransportException;
import org.hawkular.accounts.api.internal.ApplicationResources;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.model.Role;
import org.hawkular.accounts.common.ZonedDateTimeAdapter;
import org.hawkular.commons.cassandra.EmbeddedCassandraService;
import org.hawkular.commons.cassandra.EmbeddedConstants;
import org.junit.Before;

import com.datastax.driver.core.BoundStatement;
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
    OrganizationJoinRequestServiceImpl joinRequestService = new OrganizationJoinRequestServiceImpl();
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
        roleService.stmtCreateInstance = getMocked(BoundStatements.ROLES_CREATE);
        roleService.stmtGetByIdInstance = getMocked(BoundStatements.ROLES_GET_BY_ID);
        roleService.stmtGetByNameInstance = getMocked(BoundStatements.ROLES_GET_BY_NAME);

        userService.session = session;
        userService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        userService.stmtCreateInstance = getMocked(BoundStatements.USER_CREATE);
        userService.stmtAllUsersInstance = getMocked(BoundStatements.USER_ALL);
        userService.stmtGetByIdInstance = getMocked(BoundStatements.USER_GET_BY_ID);
        userService.stmtUpdateInstance = getMocked(BoundStatements.USER_UPDATE);

        permissionService.session = session;
        permissionService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        permissionService.operationService = operationService;
        permissionService.roleService = roleService;
        permissionService.stmtGetByIdInstance = getMocked(BoundStatements.PERMISSION_GET_BY_ID);
        permissionService.stmtGetByOperationInstance = getMocked(BoundStatements.PERMISSIONS_GET_BY_OPERATION);
        permissionService.stmtCreateInstance = getMocked(BoundStatements.PERMISSION_CREATE);
        permissionService.stmtDeleteInstance = getMocked(BoundStatements.PERMISSION_DELETE);

        operationService.session = session;
        operationService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        operationService.stmtGetByNameInstance = getMocked(BoundStatements.OPERATION_GET_BY_NAME);
        operationService.stmtGetByIdInstance = getMocked(BoundStatements.OPERATION_GET_BY_ID);
        operationService.stmtCreateInstance = getMocked(BoundStatements.OPERATION_CREATE);
        operationService.roleService = roleService;
        operationService.permissionService = permissionService;

        resourceService.session = session;
        resourceService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        resourceService.personaResourceRoleService = personaResourceRoleService;
        resourceService.personaService = personaService;
        resourceService.stmtGetByIdInstance = getMocked(BoundStatements.RESOURCE_GET_BY_ID);
        resourceService.stmtGetByPersonaInstance = getMocked(BoundStatements.RESOURCE_GET_BY_PERSONA);
        resourceService.stmtCreateInstance = getMocked(BoundStatements.RESOURCE_CREATE);
        resourceService.stmtTransferInstance = getMocked(BoundStatements.RESOURCE_TRANSFER);

        personaResourceRoleService.session = session;
        personaResourceRoleService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        personaResourceRoleService.resourceService= resourceService;
        personaResourceRoleService.personaService = personaService;
        personaResourceRoleService.roleService = roleService;
        personaResourceRoleService.stmtCreateInstance = getMocked(BoundStatements.PRR_CREATE);
        personaResourceRoleService.stmtGetByIdInstance = getMocked(BoundStatements.PRR_GET_BY_ID);
        personaResourceRoleService.stmtGetByPersonaInstance = getMocked(BoundStatements.PRR_GET_BY_PERSONA);
        personaResourceRoleService.stmtGetByResourceInstance = getMocked(BoundStatements.PRR_GET_BY_RESOURCE);
        personaResourceRoleService.stmtRemoveInstance = getMocked(BoundStatements.PRR_REMOVE);

        membershipService.session = session;
        membershipService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        membershipService.resourceService= resourceService;
        membershipService.personaService = personaService;
        membershipService.organizationService = organizationService;
        membershipService.roleService = roleService;
        membershipService.stmtGetByIdInstance = getMocked(BoundStatements.MEMBERSHIP_GET_BY_ID);
        membershipService.stmtGetByPersonaInstance = getMocked(BoundStatements.MEMBERSHIP_GET_BY_PERSONA);
        membershipService.stmtRemoveInstance = getMocked(BoundStatements.MEMBERSHIP_REMOVE);
        membershipService.stmtCreateInstance = getMocked(BoundStatements.MEMBERSHIP_CREATE);
        membershipService.stmtChangeRoleInstance = getMocked(BoundStatements.MEMBERSHIP_CHANGE_ROLE);
        membershipService.stmtGetByOrganizationInstance = getMocked(BoundStatements.MEMBERSHIP_GET_BY_ORGANIZATION);

        organizationService.session = session;
        organizationService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        organizationService.membershipService = membershipService;
        organizationService.resourceService = resourceService;
        organizationService.invitationService = invitationService;
        organizationService.personaService = personaService;
        organizationService.joinRequestService = joinRequestService;
        organizationService.stmtCreateInstance = getMocked(BoundStatements.ORGANIZATION_CREATE);
        organizationService.stmtGetByIdInstance = getMocked(BoundStatements.ORGANIZATION_GET_BY_ID);
        organizationService.stmtGetByNameInstance = getMocked(BoundStatements.ORGANIZATION_GET_BY_NAME);
        organizationService.stmtGetByOwnerInstance = getMocked(BoundStatements.ORGANIZATION_GET_BY_OWNER);
        organizationService.stmtTransferInstance = getMocked(BoundStatements.ORGANIZATION_TRANSFER);
        organizationService.stmtRemoveInstance = getMocked(BoundStatements.ORGANIZATION_REMOVE);
        organizationService.stmtGetApplyInstance = getMocked(BoundStatements.ORGANIZATION_GET_APPLY);

        invitationService.session = session;
        invitationService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        invitationService.roleService = roleService;
        invitationService.organizationService = organizationService;
        invitationService.userService = userService;
        invitationService.membershipService = membershipService;
        invitationService.stmtGetByTokenInstance = getMocked(BoundStatements.INVITATION_GET_BY_TOKEN);
        invitationService.stmtCreateInstance = getMocked(BoundStatements.INVITATIONS_CREATE);
        invitationService.stmtAcceptInstance = getMocked(BoundStatements.INVITATIONS_ACCEPT);
        invitationService.stmtDeleteInstance = getMocked(BoundStatements.INVITATIONS_DELETE);
        invitationService.stmtDispatchedInstance = getMocked(BoundStatements.INVITATIONS_DISPATCH);
        invitationService.stmtGetByOrganizationInstance = getMocked(BoundStatements.INVITATIONS_GET_BY_ORGANIZATION);

        settingsService.session = session;
        settingsService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        settingsService.userService = userService;
        settingsService.stmtGetByIdInstance = getMocked(BoundStatements.SETTINGS_GET_BY_ID);
        settingsService.stmtGetByUserInstance = getMocked(BoundStatements.SETTINGS_GET_BY_USER);
        settingsService.stmtCreateInstance = getMocked(BoundStatements.SETTINGS_CREATE);
        settingsService.stmtUpdateInstance = getMocked(BoundStatements.SETTINGS_UPDATE);

        joinRequestService.session = session;
        joinRequestService.zonedDateTimeAdapter = zonedDateTimeAdapter;
        joinRequestService.stmtRemove = getMocked(BoundStatements.JOIN_REQUEST_REMOVE);
        joinRequestService.stmtGetById = getMocked(BoundStatements.JOIN_REQUEST_GET_BY_ID);
        joinRequestService.stmtCreate = getMocked(BoundStatements.JOIN_REQUEST_CREATE);
        joinRequestService.stmtListByOrganization = getMocked(BoundStatements.JOIN_REQUEST_LIST_BY_ORGANIZATION);
        joinRequestService.stmtUpdateStatus = getMocked(BoundStatements.JOIN_REQUEST_UPDATE_STATUS);
        joinRequestService.stmtListByPersona = getMocked(BoundStatements.JOIN_REQUEST_LIST_BY_PERSONA);
        joinRequestService.resourceService = resourceService;
        joinRequestService.organizationService = organizationService;
        joinRequestService.personaService = personaService;
        joinRequestService.membershipService = membershipService;

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
        joinRequestService.superUser = superUser;
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
                    .withPort(9042)
                    .withProtocolVersion(ProtocolVersion.V3)
                    .build().connect();
        } catch (NoHostAvailableException e) {

            Path tmpDir = Files.createTempDirectory("hawkular-accounts-api-cassandra");
            System.setProperty(EmbeddedConstants.JBOSS_DATA_DIR, tmpDir.toAbsolutePath().toString());
            System.setProperty(EmbeddedConstants.HAWKULAR_BACKEND_PROPERTY,
                    EmbeddedConstants.EMBEDDED_CASSANDRA_OPTION);
            EmbeddedCassandraService service = new EmbeddedCassandraService();
            service.start();

            NoHostAvailableException lastException = null;
            final int timeoutSeconds = 60;
            for (int i = 0; i < timeoutSeconds; i++) {
                try {
                    session = new Cluster.Builder()
                            .addContactPoints("localhost")
                            .withPort(9042)
                            .withProtocolVersion(ProtocolVersion.V3)
                            .build().connect();
                    return;
                } catch (NoHostAvailableException t) {
                    lastException = t;
                }
                Thread.sleep(1000);
            }
            if (lastException != null) {
                throw lastException;
            }
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

    private Instance<BoundStatement> getMocked(BoundStatements stmtName) {
        Instance<BoundStatement> mocked = mock(Instance.class);
        when(mocked.get())
                .thenReturn(new BoundStatement(session.prepare(stmtName.getValue())));
        return mocked;
    }

    private String findPathForCassandraYaml(String pathToStart) throws IOException {
        File[] rootDirectories = File.listRoots();

        File file = new File(pathToStart);
        if (file.exists()) {
            return file.getAbsolutePath();
        } else {
            for (File root : rootDirectories) {
                String canonicalPathParent = file.getCanonicalFile().getParent();
                if (root.getPath().equals(canonicalPathParent)) {
                    return null;
                }
            }
            return findPathForCassandraYaml("../" + file.getParent() + "/" + file.getName());
        }
    }
}
