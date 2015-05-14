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
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Permission;
import org.hawkular.accounts.api.model.Role;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Juraci Paixão Kröhling
 */
public class OperationServiceImplTest extends BaseEntityManagerEnabledTest {

    OperationServiceImpl operationService = new OperationServiceImpl();
    PermissionServiceImpl permissionService = new PermissionServiceImpl();
    RoleServiceImpl roleService = new RoleServiceImpl();

    @Before
    public void setup() {
        roleService.em = entityManager;
        permissionService.em = entityManager;
        operationService.em = entityManager;
        operationService.permissionService = permissionService;
        operationService.roleService = roleService;
        RoleServiceImplTest roleTest = new RoleServiceImplTest();
        roleTest.initializeEntityManager();
        roleTest.setup();
    }

    @Test
    public void loadExistingOperationByName() {
        Operation operation = new Operation("foo-create");
        entityManager.getTransaction().begin();
        entityManager.persist(operation);
        entityManager.getTransaction().commit();

        Operation operationFromDatabase = operationService.getByName("foo-create");
        assertEquals("Operation should have been retrieved by name", operation.getId(), operationFromDatabase.getId());
    }

    @Test
    public void nullOnNonExistingOperationByName() {
        Operation operation = operationService.getByName("foo-create");
        assertNull("Operation should have been retrieved by name", operation);
    }

    @Test
    public void testBasicSetupWithImplicitRoles() {
        Operation operation = new Operation("foo-create");
        entityManager.getTransaction().begin();
        entityManager.persist(operation);
        entityManager.getTransaction().commit();

        Role monitor = roleService.getByName("Monitor");
        assertNotNull("We should have found a Monitor role", monitor);

        // on this basic operation, we should have only one role
        entityManager.getTransaction().begin();
        operationService
                .setup(operation)
                .add(monitor)
                .commit();
        entityManager.getTransaction().commit();

        Set<Role> roles = permissionService.getPermittedRoles(operation);
        assertEquals("Operation should be permitted only for all 7 roles", 7, roles.size());
        // just to make sure we don't get duplicates on the return...
        int sizeDistinctList = roles.stream().distinct().collect(Collectors.toList()).size();
        assertEquals("List had duplicate items!", 7, sizeDistinctList);
    }

    @Test
    public void testBasicSetupWithBasicRole() {
        Operation operation = new Operation("foo-create");
        Role superUser = roleService.getByName("SuperUser");

        entityManager.getTransaction().begin();
        entityManager.persist(operation);
        entityManager.getTransaction().commit();

        // on this basic operation, we should have only one role
        entityManager.getTransaction().begin();
        operationService
                .setup(operation)
                .add(superUser)
                .commit();
        entityManager.getTransaction().commit();

        Set<Role> roles = permissionService.getPermittedRoles(operation);
        assertEquals("Operation should be permitted only for super persona", 1, roles.size());
    }

    @Test
    public void clearShouldClearPreviousAdds() {
        Operation operation = new Operation("foo-create");
        Role superUser = roleService.getByName("SuperUser");
        Role auditor = roleService.getByName("Auditor");
        Role deployer = roleService.getByName("Deployer");

        entityManager.getTransaction().begin();
        entityManager.persist(operation);
        entityManager.getTransaction().commit();

        // on this basic operation, we should have only one role
        entityManager.getTransaction().begin();
        operationService
                .setup(operation)
                .add(superUser)
                .add(deployer)
                .add(auditor)
                .clear()
                .add(superUser)
                .commit();
        entityManager.getTransaction().commit();

        Set<Role> roles = permissionService.getPermittedRoles(operation);
        assertEquals("Operation should be permitted only for super persona", 1, roles.size());
    }

    @Test
    public void ensureNoopWhenRolesDontChange() {
        Operation operation = new Operation("foo-create");
        entityManager.getTransaction().begin();
        entityManager.persist(operation);
        entityManager.getTransaction().commit();

        Role superUser = roleService.getByName("SuperUser");
        assertNotNull("We should have found a SuperUser role", superUser);

        // on this basic operation, we should have only one role
        entityManager.getTransaction().begin();
        operationService
                .setup(operation)
                .add(superUser)
                .commit();
        entityManager.getTransaction().commit();

        Set<Permission> permissions = permissionService.getPermissionsForOperation(operation);
        assertEquals("There should be only one permission", 1, permissions.size());
        Permission permission = permissions.stream().findFirst().get();

        entityManager.getTransaction().begin();
        operationService
                .setup(operation)
                .add(superUser)
                .commit();
        entityManager.getTransaction().commit();

        Set<Permission> permissionsAfter = permissionService.getPermissionsForOperation(operation);
        assertEquals("There should be only one permission after a noop", 1, permissionsAfter.size());
        Permission permissionAfter = permissionsAfter.stream().findFirst().get();

        assertEquals("The permissions should be the same", permission, permissionAfter);
    }

    @Test
    public void ensureClearingResetsStateOfRoles() {
        Operation operation = new Operation("foo-create");
        entityManager.getTransaction().begin();
        entityManager.persist(operation);
        entityManager.getTransaction().commit();

        Role superUser = roleService.getByName("SuperUser");
        assertNotNull("We should have found a SuperUser role", superUser);

        // on this basic operation, we should have only one role
        entityManager.getTransaction().begin();
        operationService
                .setup(operation)
                .add(superUser)
                .commit();
        entityManager.flush();
        entityManager.getTransaction().commit();

        Set<Permission> permissions = permissionService.getPermissionsForOperation(operation);
        assertEquals("There should be only one permission", 1, permissions.size());
        Permission permission = permissions.stream().findFirst().get();

        entityManager.getTransaction().begin();
        operationService
                .setup(operation)
                .clear()
                .add(superUser)
                .commit();
        entityManager.flush();
        entityManager.getTransaction().commit();

        Set<Permission> permissionsAfter = permissionService.getPermissionsForOperation(operation);
        assertEquals("There should be only one permission after a noop", 1, permissionsAfter.size());
        Permission permissionAfter = permissionsAfter.stream().findFirst().get();

        assertNotEquals("The permissions should *not* be the same when a clear operation is involved",
                permission,
                permissionAfter);
    }

}
