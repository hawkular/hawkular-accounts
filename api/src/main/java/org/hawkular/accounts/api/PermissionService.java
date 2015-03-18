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
import org.hawkular.accounts.api.model.Permission;
import org.hawkular.accounts.api.model.Role;

import java.util.Set;

/**
 * Service providing access to permission-related queries. Can be injected via CDI into managed beans as follows:
 * <p>
 *     <pre>
 *         &#64;Inject PermissionService permissionService;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.
 *
 * @author Juraci Paixão Kröhling
 */
public interface PermissionService {

    /**
     * Provides a list of roles that have permission to perform the given operation.
     *
     * @param operation    the operation
     * @return a list of roles
     */
    Set<Role> getPermittedRoles(Operation operation);

    /**
     * Provides a list of Permissions that exists for the given operation
     * @param operation    the operation
     * @return a list of permissions
     */
    Set<Permission> getPermissionsForOperation(Operation operation);
}
