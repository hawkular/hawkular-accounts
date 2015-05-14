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
package org.hawkular.accounts.api;

import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Role;

import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Service intended to manage {@link Operation} entities. Can be injected via CDI into managed beans as follows:
 * <p>
 *     <pre>
 *         &#64;Inject OperationService operationService;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.
 *
 * @author Juraci Paixão Kröhling
 */
public interface OperationService {

    /**
     * Retrieves the {@link Operation} object for the given name
     *
     * @param name    unique operation name
     * @throws IllegalStateException if more than one operation exists for the given name
     * @return the Operation
     */
    Operation getByName(String name);

    /**
     * Returns a builder-style instance for fluidly assigning permitted roles for a given operation.
     * @return a new {@link Setup} instance.
     */
    Setup setup(Operation operation);

    /**
     * Returns a builder-style instance for fluidly assigning permitted roles for an operation with the given name.
     * <p>
     * If no operations are found by that name, a new one is created.
     * </p>
     * @param operationName    the operation name
     * @return a builder suitable for adding new roles to a given operation.
     */
    Setup setup(String operationName);

    /**
     * Convenience builder-style interface for setting up operations with the required roles. When adding roles, all
     * implicit roles are also included. For instance, if a system has two roles, "SuperUser" and "Monitor", and
     * "SuperUser" includes all permissions from "Monitor", then adding "SuperUser" will also add "Monitor".
     * Note that changes are effective only when the {@link #commit()} is called.
     * <p>
     * The following example could be used for setting up four operations:
     *     <pre>
     *        operationService
     *          .setup("sample-create")
     *            .add("Monitor") // adds not only Monitor, but all roles that "include" Monitor
     *          .commit()
     *
     *          .setup("sample-read")
     *            .add("Maintainer") // adds a role with the name "Maintainer" and all roles that include it
     *          .commit()
     *
     *          .setup("sample-delete")
     *            .add(superUser) // using a Role instance
     *            .add(auditor) // using a Role instance
     *          .commit()
     *
     *          .setup("sample-update")
     *            .add(maintainer, superUser, auditor) // using multiple Role instances at once
     *          .commit();
     *     </pre>
     * </p>
     *
     * @author Juraci Paixão Kröhling
     */
    interface Setup {
        /**
         * Adds a new role for the current operation
         * @param role    the role to add, cannot be null
         * @return this
         * @throws IllegalArgumentException if the role is null
         */
        Setup add(Role role);

        /**
         * Adds a new role for the current operation. The role is retrieved based on its name. If a role with the
         * given name is not found, an exception is thrown.
         *
         * @param roleName    the role name to be retrieved and to added to the operation
         * @return this
         * @throws IllegalArgumentException if a role with the given name is not found
         */
        Setup add(String roleName);

        /**
         * Adds new roles for the current operation with identical semantics as {@link #add(Role)}.
         *
         * @param role1    the first role to add
         * @param role2    the second role to add
         * @return this
         * @see #add(Role)
         */
        Setup add(Role role1, Role role2);

        /**
         * Adds new roles for the current operation with identical semantics as {@link #add(Role)}.
         *
         * @param role1    the first role to add
         * @param role2    the second role to add
         * @param role3    the third role to add
         * @return this
         * @see #add(Role)
         */
        Setup add(Role role1, Role role2, Role role3);

        /**
         * Adds new roles for the current operation with identical semantics as {@link #add(Role)}.
         *
         * @param roles    the roles to add
         * @return this
         * @see #add(Role)
         */
        Setup add(Role... roles);

        /**
         * Removes all current roles from the operation. Useful if it's known that the current set of roles is
         * invalid and a new set is to be specified.
         *
         * @return this
         */
        Setup clear();

        /**
         * Instructs the builder that the setup has finished and changes are effectively applied.
         *
         * @return the Operation related to this setup
         */
        OperationService commit();
    }

    /**
     * CDI producer method for Operation beans annotated with
     * {@link NamedOperation}. This method is not intended to be called by
     * consumers of the API.
     *
     * @param injectionPoint    the CDI InjectionPoint
     * @return the operation for the name on the annotation
     */
    Operation produceOperationByName(InjectionPoint injectionPoint);
}
