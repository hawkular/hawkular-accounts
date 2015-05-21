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
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Permission;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Juraci Paixão Kröhling
 */
public class PermissionCheckerImplTest extends BaseEntityManagerEnabledTest {

    PermissionCheckerImpl permissionChecker = new PermissionCheckerImpl();
    PermissionServiceImpl permissionService = new PermissionServiceImpl();
    PersonaServiceImpl personaService = new PersonaServiceImpl();
    OrganizationMembershipServiceImpl membershipService = new OrganizationMembershipServiceImpl();
    OrganizationServiceImpl organizationService = new OrganizationServiceImpl();

    @Before
    public void setup() {
        permissionService.em = entityManager;

        organizationService.em = entityManager;

        membershipService.em = entityManager;

        personaService.em = entityManager;
        personaService.membershipService = membershipService;
        personaService.organizationService = organizationService;

        permissionChecker.permissionService = permissionService;
        permissionChecker.personaService = personaService;
    }

    @Before
    public void baseData() {
        // basis system data
        entityManager.getTransaction().begin();
        entityManager.persist(metricsCreate);
        entityManager.persist(metricsRead);
        entityManager.persist(metricsUpdate);
        entityManager.persist(metricsDelete);

        entityManager.persist(monitor);
        entityManager.persist(operator);
        entityManager.persist(maintainer);
        entityManager.persist(deployer);
        entityManager.persist(administrator);
        entityManager.persist(auditor);
        entityManager.persist(superUser);

        entityManager.persist(maintainerCreateMetric);
        entityManager.persist(administratorCreateMetric);
        entityManager.persist(superUserCreateMetric);
        entityManager.persist(superUserOnlyOperation);

        entityManager.persist(monitorReadMetric);
        entityManager.persist(operatorReadMetric);
        entityManager.persist(maintainerReadMetric);
        entityManager.persist(deployerReadMetric);
        entityManager.persist(administratorReadMetric);
        entityManager.persist(auditorReadMetric);
        entityManager.persist(superUserReadMetric);
        entityManager.persist(superUserOnlyPermission);

        entityManager.getTransaction().commit();
    }

    @Test
    public void userHasDirectPermissionsOnResource() {
        entityManager.getTransaction().begin();
        // persona registers himself
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());

        // persona creates a resource, he's the super persona of this resource
        Resource resource = new Resource(jdoe);
        PersonaResourceRole personaResourceRole = new PersonaResourceRole(jdoe, superUser, resource);
        entityManager.persist(jdoe);
        entityManager.persist(resource);
        entityManager.persist(personaResourceRole);
        entityManager.getTransaction().commit();

        permissionChecker.isAllowedTo(metricsCreate, resource, jdoe);
    }

    @Test
    public void userBelongsToOrganizationThatHasPermissionsOnResource() {
        entityManager.getTransaction().begin();
        // persona jdoe registers himself
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());

        // persona jdoe creates an organization
        Organization acme = new Organization(jdoe);

        // persona jsmith registers himself
        HawkularUser jsmith = new HawkularUser(UUID.randomUUID().toString());

        // persona jsmith is invited to be a "Administrator" at "Acme"
        OrganizationMembership membership = new OrganizationMembership(acme, jsmith, superUser);

        // jdoe creates a resource as acme, acme is has all rights on this resource, including SuperUser and
        // Administrator
        Resource resource = new Resource(acme);
        PersonaResourceRole acmeSuperUserOnResource = new PersonaResourceRole(acme, superUser, resource);
        PersonaResourceRole acmeAdministratorOnResource = new PersonaResourceRole(acme, administrator, resource);

        entityManager.persist(jdoe);
        entityManager.persist(acme);
        entityManager.persist(jsmith);
        entityManager.persist(membership);
        entityManager.persist(resource);
        entityManager.persist(acmeSuperUserOnResource);
        entityManager.persist(acmeAdministratorOnResource);
        entityManager.getTransaction().commit();

        // persona jsmith should be able to create metrics on the given resource, since he's an administrator on acme
        assertTrue(permissionChecker.isAllowedTo(metricsCreate, resource, jsmith));
    }

    @Test
    public void resourceCreatedViaServiceHasOwnerWhoIsAlsoSuperUser() {
        ResourceServiceImpl resourceService = new ResourceServiceImpl();
        resourceService.em = entityManager;
        resourceService.superUser = superUser;

        entityManager.getTransaction().begin();
        // persona jdoe registers himself
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
        entityManager.persist(jdoe);
        entityManager.getTransaction().commit();

        // jdoe creates a resource as acme, acme is has all rights on this resource, including SuperUser and
        // Administrator
        entityManager.getTransaction().begin();
        String id = UUID.randomUUID().toString();
        Resource resource = resourceService.create(id, jdoe);
        entityManager.getTransaction().commit();

        assertTrue("Owner is super persona.", permissionChecker.isAllowedTo(superUserOnlyOperation, resource, jdoe));
    }

    @Test
    public void directlyCreatedResourceHasOwnerWithFullPermission() {
        entityManager.getTransaction().begin();
        // persona jdoe registers himself
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
        Resource resource = new Resource(jdoe);
        entityManager.persist(jdoe);
        entityManager.persist(resource);
        entityManager.getTransaction().commit();

        Operation operation = new Operation("doesnt-matter");

        assertTrue("Owner is always allowed.", permissionChecker.isAllowedTo(operation, resource, jdoe));
    }

    @Test
    public void userBelongsToOrganizationThatHasPermissionsOnResourceButHasInsufficientRoles() {
        entityManager.getTransaction().begin();
        // persona jdoe registers himself
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());

        // persona jdoe creates an organization
        Organization acme = new Organization(jdoe);

        // persona jsmith registers himself
        HawkularUser jsmith = new HawkularUser(UUID.randomUUID().toString());

        // persona jsmith is invited to be a "Monitor" at "Acme"
        OrganizationMembership membership = new OrganizationMembership(acme, jsmith, monitor);

        // jdoe creates a resource as acme, acme is has all rights on this resource, including SuperUser and
        // Administrator
        Resource resource = new Resource(acme);
        PersonaResourceRole acmeSuperUserOnResource = new PersonaResourceRole(acme, superUser, resource);
        PersonaResourceRole acmeAdministratorOnResource = new PersonaResourceRole(acme, administrator, resource);
        PersonaResourceRole acmeMonitorOnResource = new PersonaResourceRole(acme, monitor, resource);

        entityManager.persist(jdoe);
        entityManager.persist(acme);
        entityManager.persist(jsmith);
        entityManager.persist(membership);
        entityManager.persist(resource);
        entityManager.persist(acmeSuperUserOnResource);
        entityManager.persist(acmeAdministratorOnResource);
        entityManager.persist(acmeMonitorOnResource);
        entityManager.getTransaction().commit();

        // persona jsmith should be able to create metrics on the given resource, since he's an administrator on acme
        assertFalse(permissionChecker.isAllowedTo(metricsCreate, resource, jsmith));
    }

    @Test
    public void userIsOwnerOfSubResource() {
        entityManager.getTransaction().begin();
        // persona jdoe registers himself
        HawkularUser jdoe = new HawkularUser(UUID.randomUUID().toString());
        Resource resourceT = new Resource(jdoe);
        Resource resourceE = new Resource(resourceT);
        PersonaResourceRole resourceRole = new PersonaResourceRole(jdoe, superUser, resourceT);

        entityManager.persist(jdoe);
        entityManager.persist(resourceT);
        entityManager.persist(resourceE);
        entityManager.persist(resourceRole);
        entityManager.getTransaction().commit();

        assertTrue(permissionChecker.isAllowedTo(metricsCreate, resourceE, jdoe));
    }


    Operation metricsCreate = new Operation("metric-create");
    Operation metricsRead = new Operation("metric-read");
    Operation metricsUpdate = new Operation("metric-update");
    Operation metricsDelete = new Operation("metric-delete");
    Operation superUserOnlyOperation = new Operation("superUserOnlyOperation");

    Role monitor = new Role("Monitor", "Has the fewest permissions. Only read configuration and current runtime " +
            "state, No access to sensitive resources or data or audit logging resources");

    Role operator = new Role("Operator", "All permissions of Monitor. Can modify the runtime state, e.g. reload " +
            "or shutdown the server, pause/resume JMS destination, flush database connection pool. Does not have " +
            "permission to modify persistent state.");

    Role maintainer = new Role("Maintainer", "All permissions of Operator. Can modify the persistent state, e.g. " +
            "deploy an application, setting up new data sources, add a JMS destination");

    Role deployer = new Role("Deployer", "All permissions of Maintainer. Permission is restricted to applications" +
            " only, cannot make changes to container configuration");

    Role administrator = new Role("Administrator", "All permissions of Maintainer. View and modify sensitive data" +
            " such as access control system.  No access to administrative audit logging system.");

    Role auditor = new Role("Auditor", "All permissions of Monitor. View and modify resources to administrative " +
            "audit logging system. Cannot modify sensitive resources or data outside auditing, can read any " +
            "sensitive data");

    Role superUser = new Role("SuperUser", "Has all the permissions. Equivalent to administrator in previous " +
            "versions.");

    Permission maintainerCreateMetric = new Permission(metricsCreate, maintainer);
    Permission administratorCreateMetric = new Permission(metricsCreate, administrator);
    Permission superUserCreateMetric = new Permission(metricsCreate, superUser);
    Permission superUserOnlyPermission = new Permission(superUserOnlyOperation, superUser);

    Permission monitorReadMetric = new Permission(metricsCreate, superUser);
    Permission operatorReadMetric = new Permission(metricsCreate, superUser);
    Permission maintainerReadMetric = new Permission(metricsCreate, superUser);
    Permission deployerReadMetric = new Permission(metricsCreate, superUser);
    Permission administratorReadMetric = new Permission(metricsCreate, superUser);
    Permission auditorReadMetric = new Permission(metricsCreate, superUser);
    Permission superUserReadMetric = new Permission(metricsCreate, superUser);
}
