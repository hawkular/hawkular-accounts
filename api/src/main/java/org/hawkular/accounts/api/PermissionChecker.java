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
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;

/**
 * Central part of the API, allowing a component to perform permission checking of users against resources. Can be
 * injected via CDI into managed beans as follows:
 * <p>
 *     <pre>
 *         &#64;Inject PermissionChecker permissionChecker;
 *     </pre>
 * </p>
 * Concrete implementations do not hold any state, but it's advised to get an instance through CDI or as an EJB.
 *
 * @author Juraci Paixão Kröhling
 */
public interface PermissionChecker {

    /**
     * Checks whether the given {@link Persona} has access to perform {@link Operation} on the given {@link Resource}.
     * @param operation    the operation that is to be performed. Example: "create-metric".
     * @param resource     the resource onto which the operation is to be performed. Example: "cpu-usage".
     * @param persona      the persona that is about to perform the operation. Example: "jdoe".
     * @return true if the given persona is allowed to perform the operation on the resource.
     * @throws IllegalArgumentException if any of the parameters is null
     */
    boolean isAllowedTo(Operation operation, Resource resource, Persona persona);

    /**
     * Checks whether the given {@link Persona} has access to perform {@link Operation} on the given {@link Resource}.
     * Resource here is referenced by its ID.
     * @param operation    the operation that is to be performed. Example: "create-metric".
     * @param resourceId   the ID for the resource onto which the operation is to be performed. Example: "cpu-usage".
     * @param persona      the persona that is about to perform the operation. Example: "jdoe".
     * @return true if the given persona is allowed to perform the operation on the resource.
     * @see #isAllowedTo(Operation, Resource, Persona)
     * @throws IllegalArgumentException if any of the parameters is null or if the resourceId doesn't references an
     * existing resource.
     */
    boolean isAllowedTo(Operation operation, String resourceId, Persona persona);

    /**
     * Checks whether the current {@link Persona} has access to perform {@link Operation} on the given {@link Resource}.
     * @param operation    the operation that is to be performed. Example: "create-metric".
     * @param resource     the resource onto which the operation is to be performed. Example: "cpu-usage".
     * @return true if the current persona is allowed to perform the operation on the resource.
     * @throws IllegalArgumentException if any of the parameters is null
     * @see #isAllowedTo(Operation, Resource, Persona)
     */
    boolean isAllowedTo(Operation operation, Resource resource);

    /**
     * Checks whether the current {@link Persona} has access to perform {@link Operation} on the given
     * {@link Resource}. Resource here is referenced by its ID.
     * @param operation    the operation that is to be performed. Example: "create-metric".
     * @param resourceId   the ID for the resource onto which the operation is to be performed. Example: "cpu-usage".
     * @return true if the current persona is allowed to perform the operation on the resource.
     * @see #isAllowedTo(Operation, Resource, Persona)
     * @throws IllegalArgumentException if any of the parameters is null or if the resourceId doesn't references an
     * existing resource.
     */
    boolean isAllowedTo(Operation operation, String resourceId);
}
