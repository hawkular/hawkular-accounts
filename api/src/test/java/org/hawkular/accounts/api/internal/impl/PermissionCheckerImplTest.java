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
public class PermissionCheckerImplTest extends SessionEnabledTest {
    @Before
    public void baseData() {
        // basis system data
        metricsCreate = operationService.getOrCreateByName("metric-create");
        metricsRead = operationService.getOrCreateByName("metric-read");
        metricsUpdate = operationService.getOrCreateByName("metric-update");
        metricsDelete = operationService.getOrCreateByName("metric-delete");

        superUserOnlyOperation = operationService.getOrCreateByName("superUserOnlyOperation");

        maintainerCreateMetric = permissionService.create(metricsCreate, maintainer);
        administratorCreateMetric = permissionService.create(metricsCreate, administrator);
        superUserCreateMetric = permissionService.create(metricsCreate, superUser);
        superUserOnlyPermission = permissionService.create(superUserOnlyOperation, superUser);

        monitorReadMetric = permissionService.create(metricsCreate, superUser);
        operatorReadMetric = permissionService.create(metricsCreate, superUser);
        maintainerReadMetric = permissionService.create(metricsCreate, superUser);
        deployerReadMetric = permissionService.create(metricsCreate, superUser);
        administratorReadMetric = permissionService.create(metricsCreate, superUser);
        auditorReadMetric = permissionService.create(metricsCreate, superUser);
        superUserReadMetric = permissionService.create(metricsCreate, superUser);
    }

    @Test
    public void userHasDirectPermissionsOnResource() {
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), jdoe);

        permissionChecker.isAllowedTo(metricsCreate, resource, jdoe);
    }

    @Test
    public void userBelongsToOrganizationThatHasPermissionsOnResource() {
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

        // persona jsmith should be able to create metrics on the given resource, since he's an administrator on acme
        assertTrue(permissionChecker.isAllowedTo(metricsCreate, resource, jsmith));
    }

    @Test
    public void resourceCreatedViaServiceHasOwnerWhoIsAlsoSuperUser() {
        // persona jdoe registers himself
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());

        // jdoe creates a resource as acme, acme is has all rights on this resource, including SuperUser and
        // Administrator
        String id = UUID.randomUUID().toString();
        Resource resource = resourceService.create(id, jdoe);

        assertTrue("Owner is super persona.", permissionChecker.isAllowedTo(superUserOnlyOperation, resource, jdoe));
    }

    @Test
    public void directlyCreatedResourceHasOwnerWithFullPermission() {
        // persona jdoe registers himself
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resource = resourceService.create(UUID.randomUUID().toString(), jdoe);

        Operation operation = operationService.create("doesnt-matter");

        assertTrue("Owner is always allowed.", permissionChecker.isAllowedTo(operation, resource, jdoe));
    }

    @Test
    public void userBelongsToOrganizationThatHasPermissionsOnResourceButHasInsufficientRoles() {
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

        // persona jsmith should not be able to create metrics on the given resource, since he's only a monitor on acme
        assertFalse(permissionChecker.isAllowedTo(metricsCreate, resource, jsmith));
    }

    @Test
    public void userIsOwnerOfSubResource() {
        // persona jdoe registers himself
        HawkularUser jdoe = userService.getOrCreateById(UUID.randomUUID().toString());
        Resource resourceT = resourceService.create(UUID.randomUUID().toString(), jdoe);
        Resource resourceE = resourceService.create(UUID.randomUUID().toString(), resourceT);

        assertTrue(permissionChecker.isAllowedTo(metricsCreate, resourceE, jdoe));
    }

    Operation metricsCreate;
    Operation metricsRead;
    Operation metricsUpdate;
    Operation metricsDelete;
    Operation superUserOnlyOperation;

    Permission maintainerCreateMetric;
    Permission administratorCreateMetric;
    Permission superUserCreateMetric;
    Permission superUserOnlyPermission;

    Permission monitorReadMetric;
    Permission operatorReadMetric;
    Permission maintainerReadMetric;
    Permission deployerReadMetric;
    Permission administratorReadMetric;
    Permission auditorReadMetric;
    Permission superUserReadMetric;
}
