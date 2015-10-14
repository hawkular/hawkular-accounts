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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.hawkular.accounts.api.model.Role;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class RoleServiceImplTest extends BaseServicesTest {
    @Test
    public void implicitUserRolesForSuperUser() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles(superUser);
        assertEquals("SuperUser should have all 6 other roles as implicit roles", 6, implicitRoles.size());
        assertTrue(implicitRoles.contains(monitor));
        assertTrue(implicitRoles.contains(operator));
        assertTrue(implicitRoles.contains(maintainer));
        assertTrue(implicitRoles.contains(deployer));
        assertTrue(implicitRoles.contains(administrator));
        assertTrue(implicitRoles.contains(auditor));
    }

    @Test
    public void implicitUserRolesForMonitor() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles(monitor);
        assertEquals("Monitor should have no implicit roles", 0, implicitRoles.size());
    }

    @Test
    public void implicitUserRolesForAuditor() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles(auditor);
        assertEquals("Auditor should have 1 other role as implicit roles", 1, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Monitor")));
    }

    @Test
    public void implicitUserRolesForOperator() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles(operator);
        assertEquals("Operator should have 1 other role as implicit roles", 1, implicitRoles.size());
        assertTrue(implicitRoles.contains(roleService.getByName("Monitor")));
    }

    @Test
    public void implicitUserRolesForMaintainer() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles(maintainer);
        assertEquals("Maintainer should have 2 other roles as implicit roles", 2, implicitRoles.size());
        assertTrue(implicitRoles.contains(monitor));
        assertTrue(implicitRoles.contains(operator));
    }

    @Test
    public void implicitUserRolesForDeployer() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles(deployer);
        assertEquals("Deployer should have 3 other roles as implicit roles", 3, implicitRoles.size());
        assertTrue(implicitRoles.contains(monitor));
        assertTrue(implicitRoles.contains(operator));
        assertTrue(implicitRoles.contains(maintainer));
    }

    @Test
    public void implicitUserRolesForAdministrator() {
        Set<Role> implicitRoles = roleService.getImplicitUserRoles(administrator);
        assertEquals("Administrator should have 3 other roles as implicit roles", 3, implicitRoles.size());
        assertTrue(implicitRoles.contains(monitor));
        assertTrue(implicitRoles.contains(operator));
        assertTrue(implicitRoles.contains(maintainer));
    }

    @Test
    public void implicitPermittedRolesForSuperUser() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles(superUser);
        assertEquals("No other roles can execute operations marked with SuperUser.", 0, implicitRoles.size());
    }

    @Test
    public void implicitPermittedRolesForMonitor() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles(monitor);
        assertEquals("Operator means that 6 other roles have also access to the operation.", 6, implicitRoles.size());
        assertTrue(implicitRoles.contains(operator));
        assertTrue(implicitRoles.contains(maintainer));
        assertTrue(implicitRoles.contains(deployer));
        assertTrue(implicitRoles.contains(administrator));
        assertTrue(implicitRoles.contains(auditor));
        assertTrue(implicitRoles.contains(superUser));
    }

    @Test
    public void implicitPermittedRolesForOperator() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles(operator);
        assertEquals("Operator means that 4 other roles have also access to the operation.", 4, implicitRoles.size());
        assertTrue(implicitRoles.contains(maintainer));
        assertTrue(implicitRoles.contains(deployer));
        assertTrue(implicitRoles.contains(administrator));
        assertTrue(implicitRoles.contains(superUser));
    }

    @Test
    public void implicitPermittedRolesForMaintainer() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles(maintainer);
        assertEquals("Maintainer means that 3 other roles have also access to the operation.", 3, implicitRoles.size());
        assertTrue(implicitRoles.contains(deployer));
        assertTrue(implicitRoles.contains(administrator));
        assertTrue(implicitRoles.contains(superUser));
    }

    @Test
    public void implicitPermittedRolesForDeployer() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles(deployer);
        assertEquals("Deployer means that 1 other roles have also access to the operation.", 1, implicitRoles.size());
        assertTrue(implicitRoles.contains(superUser));
    }

    @Test
    public void implicitPermittedRolesForAdministrator() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles(administrator);
        assertEquals("Administrator means that 1 other roles have also access to the operation.", 1, implicitRoles
                .size());
        assertTrue(implicitRoles.contains(superUser));
    }

    @Test
    public void implicitPermittedRolesForAuditor() {
        Set<Role> implicitRoles = roleService.getImplicitPermittedRoles(auditor);
        assertEquals("Auditor means that 1 other roles have also access to the operation.", 1, implicitRoles.size());
        assertTrue(implicitRoles.contains(superUser));
    }

}
