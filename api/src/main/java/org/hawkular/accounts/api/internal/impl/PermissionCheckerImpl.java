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

import org.hawkular.accounts.api.PermissionChecker;
import org.hawkular.accounts.api.PermissionService;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.model.Operation;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Set;

/**
 * Main implementation of the {@link org.hawkular.accounts.api.PermissionChecker}. Consumers should get an instance of
 * this via CDI. This class should not be directly instantiated by the consumers.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class PermissionCheckerImpl implements PermissionChecker {

    @Inject
    PermissionService permissionService;

    @Inject
    PersonaService personaService;

    @Inject
    ResourceService resourceService;

    @Inject
    Persona persona;

    @Override
    public boolean isAllowedTo(Operation operation, Resource resource, Persona persona) {
        if (null == resource) {
            throw new IllegalArgumentException("Resource to be checked is invalid (null).");
        }

        if (null == operation) {
            throw new IllegalArgumentException("Operation to be checked is invalid (null).");
        }

        if (null == persona) {
            throw new IllegalArgumentException("Persona that performs the operation is invalid (null).");
        }

        if (persona.equals(resource.getPersona())) {
            // owner is always allowed
            return true;
        }

        // TODO: should we *always* add Super User to the permitted roles?
        Set<Role> permittedRoles = permissionService.getPermittedRoles(operation);
        Set<Role> personaRoles = personaService.getEffectiveRolesForResource(persona, resource);
        return personaRoles.stream().anyMatch(permittedRoles::contains);
    }

    @Override
    public boolean isAllowedTo(Operation operation, String resourceId, Persona persona) {
        Resource resource = resourceService.get(resourceId);
        return isAllowedTo(operation, resource, persona);
    }

    @Override
    public boolean isAllowedTo(Operation operation, Resource resource) {
        return isAllowedTo(operation, resource, persona);
    }

    @Override
    public boolean isAllowedTo(Operation operation, String resourceId) {
        Resource resource = resourceService.get(resourceId);
        return isAllowedTo(operation, resource);
    }
}
