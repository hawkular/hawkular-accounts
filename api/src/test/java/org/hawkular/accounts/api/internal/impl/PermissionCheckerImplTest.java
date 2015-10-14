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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.Permission;
import org.hawkular.accounts.api.model.Resource;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class PermissionCheckerImplTest extends BaseServicesTest {
    @Before
    public void baseData() {
        // basis system data
        entityManager.getTransaction().begin();
        entityManager.persist(metricsCreate);
        entityManager.persist(metricsRead);
        entityManager.persist(metricsUpdate);
        entityManager.persist(metricsDelete);

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
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), jdoe);
        entityManager.getTransaction().commit();

        permissionChecker.isAllowedTo(metricsCreate, resource, jdoe);
    }

    @Test
    public void userBelongsToOrganizationThatHasPermissionsOnResource() {
        entityManager.getTransaction().begin();
        // persona jdoe registers himself
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());

        // persona jdoe creates an organization
        Organization acme = organizationService.createOrganization("Acme", "Acme", jdoe);

        // persona jsmith registers himself
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());

        // persona jsmith is invited to be a "Administrator" at "Acme"
        Invitation invitation = invitationService.create("", jdoe, acme, administrator);
        invitationService.accept(invitation, jsmith);

        // jdoe creates a resource as acme, acme has all rights on this resource, including SuperUser and Administrator
        Resource resource = resourceService.create(UUID.randomUUID().toString(), acme);

        entityManager.getTransaction().commit();

        // persona jsmith should be able to create metrics on the given resource, since he's an administrator on acme
        assertTrue(permissionChecker.isAllowedTo(metricsCreate, resource, jsmith));
    }

    @Test
    public void resourceCreatedViaServiceHasOwnerWhoIsAlsoSuperUser() {
        entityManager.getTransaction().begin();
        // persona jdoe registers himself
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
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
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), jdoe);
        entityManager.getTransaction().commit();

        Operation operation = new Operation("doesnt-matter");

        assertTrue("Owner is always allowed.", permissionChecker.isAllowedTo(operation, resource, jdoe));
    }

    @Test
    public void userBelongsToOrganizationThatHasPermissionsOnResourceButHasInsufficientRoles() {
        entityManager.getTransaction().begin();
        // persona jdoe registers himself
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());

        // persona jdoe creates an organization
        Organization acme = organizationService.createOrganization("Acme", "Acme", jdoe);

        // persona jsmith registers himself
        HawkularUser jsmith = userService.getOrCreateById(UUID.randomUUID().toString());

        // persona jsmith is invited to be a "Monitor" at "Acme"
        Invitation invitation = invitationService.create("", jdoe, acme, monitor);
        invitationService.accept(invitation, jsmith);

        // jdoe creates a resource as acme, acme is has all rights on this resource, including SuperUser and
        // Administrator
        Resource resource = resourceService.create(UUID.randomUUID().toString(), acme);

        entityManager.persist(resource);
        entityManager.getTransaction().commit();

        // persona jsmith should not be able to create metrics on the given resource, since he's only a monitor on acme
        assertFalse(permissionChecker.isAllowedTo(metricsCreate, resource, jsmith));
    }

    @Test
    public void userIsOwnerOfSubResource() {
        entityManager.getTransaction().begin();
        // persona jdoe registers himself
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resourceT = resourceService.create(UUID.randomUUID().toString(), jdoe);
        Resource resourceE = resourceService.create(UUID.randomUUID().toString(), resourceT);
        entityManager.getTransaction().commit();

        assertTrue(permissionChecker.isAllowedTo(metricsCreate, resourceE, jdoe));
    }


    Operation metricsCreate = new Operation("metric-create");
    Operation metricsRead = new Operation("metric-read");
    Operation metricsUpdate = new Operation("metric-update");
    Operation metricsDelete = new Operation("metric-delete");
    Operation superUserOnlyOperation = new Operation("superUserOnlyOperation");

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
