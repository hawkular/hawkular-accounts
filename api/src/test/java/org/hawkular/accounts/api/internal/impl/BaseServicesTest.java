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

import org.hawkular.accounts.api.BaseEntityManagerEnabledTest;
import org.hawkular.accounts.api.model.Role;
import org.junit.Before;

/**
 * @author Juraci Paixão Kröhling
 */
public class BaseServicesTest extends BaseEntityManagerEnabledTest {
    InvitationServiceImpl invitationService = new InvitationServiceImpl();
    OperationServiceImpl operationService = new OperationServiceImpl();
    OrganizationMembershipServiceImpl membershipService = new OrganizationMembershipServiceImpl();
    OrganizationServiceImpl organizationService = new OrganizationServiceImpl();
    PermissionCheckerImpl permissionChecker = new PermissionCheckerImpl();
    PermissionServiceImpl permissionService = new PermissionServiceImpl();
    PersonaServiceImpl personaService = new PersonaServiceImpl();
    ResourceServiceImpl resourceService = new ResourceServiceImpl();
    RoleServiceImpl roleService = new RoleServiceImpl();
    UserServiceImpl userService = new UserServiceImpl();
    UserSettingsServiceImpl settingsService = new UserSettingsServiceImpl();

    Role superUser = new Role("SuperUser", "");
    Role administrator = new Role("Administrator", "");
    Role auditor = new Role("Auditor", "");
    Role deployer = new Role("Deployer", "");
    Role maintainer = new Role("Maintainer", "");
    Role operator = new Role("Operator", "");
    Role monitor = new Role("Monitor", "");

    @Before
    public void prepare() {
        entityManager.getTransaction().begin();
        entityManager.persist(superUser);
        entityManager.persist(administrator);
        entityManager.persist(auditor);
        entityManager.persist(deployer);
        entityManager.persist(maintainer);
        entityManager.persist(operator);
        entityManager.persist(monitor);
        entityManager.getTransaction().commit();

        roleService.em = entityManager;
        userService.em = entityManager;
        settingsService.em = entityManager;
        permissionService.em = entityManager;

        resourceService.em = entityManager;
        resourceService.superUser = superUser;

        operationService.em = entityManager;
        operationService.permissionService = permissionService;
        operationService.roleService = roleService;

        personaService.em = entityManager;
        personaService.membershipService = membershipService;
        personaService.organizationService = organizationService;
        personaService.userService = userService;
        personaService.resourceService = resourceService;
        personaService.roleService = roleService;

        permissionChecker.permissionService = permissionService;
        permissionChecker.personaService = personaService;
        permissionChecker.resourceService = resourceService;

        membershipService.em = entityManager;
        membershipService.resourceService = resourceService;
        membershipService.personaService = personaService;

        organizationService.em = entityManager;
        organizationService.membershipService = membershipService;
        organizationService.resourceService = resourceService;
        organizationService.superUser = superUser;
        organizationService.invitationService = invitationService;

        invitationService.em = entityManager;
        invitationService.membershipService = membershipService;
    }

}
