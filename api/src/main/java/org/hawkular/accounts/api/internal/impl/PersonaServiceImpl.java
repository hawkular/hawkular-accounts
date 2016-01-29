/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PersonaResourceRoleService;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.RoleService;
import org.hawkular.accounts.api.UserService;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

/**
 * Main implementation of the {@link org.hawkular.accounts.api.PersonaService}. Consumers should get an instance of this
 * via CDI. This class should not be directly instantiated by the consumers.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class PersonaServiceImpl implements PersonaService {
    MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    OrganizationMembershipService membershipService;

    @Inject
    OrganizationService organizationService;

    @Inject
    UserService userService;

    @Inject
    ResourceService resourceService;

    @Inject
    RoleService roleService;

    @Inject
    PersonaResourceRoleService personaResourceRoleService;

    @Inject
    private HttpServletRequest httpRequest;

    public Persona getById(UUID id) {
        return get(id.toString());
    }

    public Persona get(String id) {
        if (null == id) {
            throw new IllegalArgumentException("The provided Persona ID is invalid (null).");
        }

        // for now, we will be doing two queries: one for user, one for organization, which are the two known
        // persona types
        Persona persona;

        UUID uuid = UUID.fromString(id);
        persona = userService.getById(uuid);
        if (null == persona) {
            persona = organizationService.getById(uuid);
        }

        return persona;
    }

    @Override
    public Set<Role> getEffectiveRolesForResource(Persona persona, Resource resource) {
        if (null == persona) {
            throw new IllegalArgumentException("Missing persona (null).");
        }

        if (null == resource) {
            throw new IllegalArgumentException("Missing resource (null).");
        }

        logger.determiningEffectiveRolesForPersonaOnResource(persona.getId(), resource.getId());
        // rules:
        // if the persona has explicit roles for this resource, that's what is effective.
        // if the persona has *no* explicit roles, traverse the Organizations that this persona is part of
        // and return *all* the roles. For instance:
        // User "jdoe" is "Deployer" on "Department 1" and "Auditor" on "Department 2"
        // "Department 1" is "Maintainer" of "node1"
        // "Department 2" is "Auditor", "Administrator" and "Monitor" of "node1"
        // Therefore, jdoe is only "Auditor" of "node1".

        List<PersonaResourceRole> results = personaResourceRoleService.getByPersonaAndResource(persona, resource);
        logger.numOfDirectRolesOnResource(persona.getId(), resource.getId(), results.size());
        if (results.size() == 0) {
            logger.noDirectRolesOnResource(persona.getId(), resource.getId());
            // this means: this persona has no direct roles on the resource, let's check the organizations it belongs to
            List<Organization> organizations = organizationService.getOrganizationsForPersona(persona);

            Set<Role> roles = new HashSet<>();
            for (Organization organization : organizations) {
                logger.checkingIndirectRolesViaOrganization(persona.getId(), resource.getId(), organization.getId());
                List<OrganizationMembership> memberships =
                        membershipService.getPersonaMembershipsForOrganization(persona, organization);
                // here, we basically filter what are the minimum set of roles a persona has on a resource
                // example:
                // persona "jdoe" is "Auditor" "acme"
                // "acme" is "Monitor" and "Auditor" on "node1"
                // therefore, "jdoe" is only "Auditor" on "node1"

                Set<Role> organizationRolesForResource = getEffectiveRolesForResource(organization, resource);
                Set<Role> effectiveRoles = memberships
                        .stream()
                        // get what are the persona's roles (direct or implicit) on the organization
                        .map(membership -> {
                                    Set<Role> implicitRoles = roleService.getImplicitUserRoles(membership.getRole());
                                    implicitRoles.add(membership.getRole());
                                    return implicitRoles;
                                }
                        )
                        // flattens the stream, so that we have a stream of roles (instead of stream of Set<Role>)
                        .flatMap(Collection::stream)
                                // accept only roles that the organization has on the resource
                        .filter(organizationRolesForResource::contains)
                        // the result is: roles that the persona has on the organization, restricted to only the
                                // roles that the organization has on the resource
                        .collect(Collectors.toSet());

                logger.numOfEffectiveRolesViaOrganization(
                        persona.getId(),
                        resource.getId(),
                        organization.getId(),
                        effectiveRoles.size()
                );
                roles.addAll(effectiveRoles);
            }
            logger.totalEffectiveRolesOnResource(persona.getId(), resource.getId(), roles.size());
            return roles;
        }

        Set<Role> roles = new HashSet<>(results.size());

        // first, we add all direct roles
        roles.addAll(results.stream().map(PersonaResourceRole::getRole).collect(Collectors.toSet()));

        // then, we add the implicit roles
        roles.addAll(
                results
                        .stream()
                        .map(r -> roleService.getImplicitUserRoles(r.getRole()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())
        );

        logger.totalEffectiveRolesOnResourceWithImplicitRoles(persona.getId(), resource.getId(), roles.size());
        return roles;
    }

    @Override
    @Produces
    public Persona getCurrent() {
        String personaId = httpRequest.getHeader("Hawkular-Persona");
        if (personaId != null && !personaId.isEmpty()) {
            Persona persona = get(personaId);
            if (isAllowedToImpersonate(userService.getCurrent(), persona)) {
                return persona;
            } else {
                throw new RuntimeException("User is not allowed to impersonate this persona.");
            }
        }

        // we don't have a persona on the request, so, assume it's the current user
        return userService.getCurrent();
    }

    @Override
    public boolean isAllowedToImpersonate(HawkularUser actual, Persona toImpersonate) {
        if (actual.equals(toImpersonate)) {
            return true; // user is the persona
        }

        if (toImpersonate instanceof HawkularUser) {
            return false; // user cannot impersonate another user
        }

        // an organization is a resource
        Resource resource = resourceService.getById(toImpersonate.getIdAsUUID());
        Set<Role> roles = getEffectiveRolesForResource(actual, resource);
        return roles != null && roles.size() > 0; // at least one role is enough
    }
}
