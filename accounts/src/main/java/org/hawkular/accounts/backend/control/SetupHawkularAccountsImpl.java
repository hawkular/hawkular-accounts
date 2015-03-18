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
package org.hawkular.accounts.backend.control;

import org.hawkular.accounts.api.OperationService;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.Role;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.Set;

/**
 * Startup singleton that takes care of initializing data that we need for both the main Accounts module and for this
 * backend.
 *
 * @author Juraci Paixão Kröhling
 */
@Startup
@Singleton
@PermitAll
public class SetupHawkularAccountsImpl {
    private final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    RoleService roleService;

    @Inject
    @HawkularAccounts
    EntityManager entityManager;

    @Inject
    OperationService operationService;

    Set<Role> roles = new HashSet<>(7);

    @PostConstruct
    public void setup() {
        logger.infoStartedSetupAccounts();
        roles.add(monitor);
        roles.add(operator);
        roles.add(maintainer);
        roles.add(deployer);
        roles.add(administrator);
        roles.add(auditor);
        roles.add(superUser);
        roles.stream().forEach(this::addRoleIfDoesntExists);

        // we use the role names, as we don't know if the objects we have are persisted or ignored
        // they can be ignored if a role with the same already exists
        operationService
                .setup("organization-create")
                .add("Monitor") // means: all roles
                .commit()

                .setup("organization-read")
                .add("Maintainer")
                .commit()

                .setup("organization-delete")
                .add("Super User")
                .commit()

                .setup("organization-update")
                .add("Maintainer")
                .commit();

        logger.infoFinishedSetupAccounts();
    }

    private void addRoleIfDoesntExists(Role role) {
        if (null == roleService.getByName(role.getName())) {
            entityManager.persist(role);
        }
        entityManager.flush();
    }

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

    Role superUser = new Role("Super User", "Has all the permissions. Equivalent to administrator in previous " +
            "versions.");

}
