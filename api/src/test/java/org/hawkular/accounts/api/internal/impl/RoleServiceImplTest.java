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
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Juraci Paixão Kröhling
 */
public class RoleServiceImplTest extends BaseEntityManagerEnabledTest {

    private RoleServiceImpl roleService = new RoleServiceImpl();

    @Before
    public void setup() {
        roleService.em = entityManager;

        Role superUser = new Role("SuperUser", "");
        Role administrator = new Role("Administrator", "");
        Role auditor = new Role("Auditor", "");
        Role deployer = new Role("Deployer", "");
        Role maintainer = new Role("Maintainer", "");
        Role operator = new Role("Operator", "");
        Role monitor = new Role("Monitor", "");

        entityManager.getTransaction().begin();
        entityManager.persist(superUser);
        entityManager.persist(administrator);
        entityManager.persist(auditor);
        entityManager.persist(deployer);
        entityManager.persist(maintainer);
        entityManager.persist(operator);
        entityManager.persist(monitor);
        entityManager.getTransaction().commit();
    }

    @Test
    public void implicitUserRolesForSuperUser() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles("SuperUser");
        assertEquals("SuperUser should have all 6 other roles as implicit roles", 6, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Monitor")));
        assertTrue(implicitRoles.contains(roleService.getByName("Operator")));
        assertTrue(implicitRoles.contains(roleService.getByName("Maintainer")));
        assertTrue(implicitRoles.contains(roleService.getByName("Deployer")));
        assertTrue(implicitRoles.contains(roleService.getByName("Administrator")));
        assertTrue(implicitRoles.contains(roleService.getByName("Auditor")));
    }

    @Test
    public void implicitUserRolesForMonitor() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles("Monitor");
        assertEquals("Monitor should have no implicit roles", 0, implicitRoles.size());
    }

    @Test
    public void implicitUserRolesForAuditor() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles("Auditor");
        assertEquals("Auditor should have 1 other role as implicit roles", 1, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Monitor")));
    }

    @Test
    public void implicitUserRolesForOperator() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles("Operator");
        assertEquals("Operator should have 1 other role as implicit roles", 1, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Monitor")));
    }

    @Test
    public void implicitUserRolesForMaintainer() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles("Maintainer");
        assertEquals("Maintainer should have 2 other roles as implicit roles", 2, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Monitor")));
        assertTrue(implicitRoles.contains(roleService.getByName("Operator")));
    }

    @Test
    public void implicitUserRolesForDeployer() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles("Deployer");
        assertEquals("Deployer should have 3 other roles as implicit roles", 3, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Monitor")));
        assertTrue(implicitRoles.contains(roleService.getByName("Operator")));
        assertTrue(implicitRoles.contains(roleService.getByName("Maintainer")));
    }

    @Test
    public void implicitUserRolesForAdministrator() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles("Administrator");
        assertEquals("Administrator should have 3 other roles as implicit roles", 3, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Monitor")));
        assertTrue(implicitRoles.contains(roleService.getByName("Operator")));
        assertTrue(implicitRoles.contains(roleService.getByName("Maintainer")));
    }

    @Test
    public void implicitPermittedRolesForSuperUser() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles("SuperUser");
        assertEquals("No other roles can execute operations marked with SuperUser.", 0, implicitRoles.size());
    }

    @Test
    public void implicitPermittedRolesForMonitor() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles("Monitor");
        assertEquals("Operator means that 6 other roles have also access to the operation.", 6, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Operator")));
        assertTrue(implicitRoles.contains(roleService.getByName("Maintainer")));
        assertTrue(implicitRoles.contains(roleService.getByName("Deployer")));
        assertTrue(implicitRoles.contains(roleService.getByName("Administrator")));
        assertTrue(implicitRoles.contains(roleService.getByName("Auditor")));
        assertTrue(implicitRoles.contains(roleService.getByName("SuperUser")));
    }

    @Test
    public void implicitPermittedRolesForOperator() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles("Operator");
        assertEquals("Operator means that 4 other roles have also access to the operation.", 4, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Maintainer")));
        assertTrue(implicitRoles.contains(roleService.getByName("Deployer")));
        assertTrue(implicitRoles.contains(roleService.getByName("Administrator")));
        assertTrue(implicitRoles.contains(roleService.getByName("SuperUser")));
    }

    @Test
    public void implicitPermittedRolesForMaintainer() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles("Maintainer");
        assertEquals("Maintainer means that 3 other roles have also access to the operation.", 3, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Deployer")));
        assertTrue(implicitRoles.contains(roleService.getByName("Administrator")));
        assertTrue(implicitRoles.contains(roleService.getByName("SuperUser")));
    }

    @Test
    public void implicitPermittedRolesForDeployer() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles("Deployer");
        assertEquals("Deployer means that 1 other roles have also access to the operation.", 1, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("SuperUser")));
    }

    @Test
    public void implicitPermittedRolesForAdministrator() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles("Administrator");
        assertEquals("Administrator means that 1 other roles have also access to the operation.", 1, implicitRoles
                .size());
        assertTrue(implicitRoles.contains(roleService.getByName("SuperUser")));
    }

    @Test
    public void implicitPermittedRolesForAuditor() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles("Auditor");
        assertEquals("Auditor means that 1 other roles have also access to the operation.", 1, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("SuperUser")));
    }

}
