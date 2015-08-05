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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.OrganizationService;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.UserService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.PersonaResourceRole;
import org.hawkular.accounts.api.model.PersonaResourceRole_;
import org.hawkular.accounts.api.model.Persona_;
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

    @Inject
    @HawkularAccounts
    EntityManager em;

    @Inject
    OrganizationMembershipService membershipService;

    @Inject
    OrganizationService organizationService;

    @Inject
    UserService userService;

    @Inject
    private HttpServletRequest httpRequest;

    public Persona get(String id) {
        if (null == id) {
            throw new IllegalArgumentException("The provided Persona ID is invalid (null).");
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Persona> query = builder.createQuery(Persona.class);
        Root<Persona> root = query.from(Persona.class);
        query.select(root);
        query.where(builder.equal(root.get(Persona_.id), id));

        List<Persona> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            return results.get(0);
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one owner found for ID " + id);
        }

        return null;
    }

    @Override
    public Set<Role> getEffectiveRolesForResource(Persona persona, Resource resource) {
        // rules:
        // if the persona has explicit roles for this resource, that's what is effective.
        // if the persona has *no* explicit roles, traverse the Organizations that this persona is part of
        // and return *all* the roles. For instance:
        // User "jdoe" is "Deployer" on "Department 1" and "Auditor" on "Department 2"
        // "Department 1" is "Maintainer" of "node1"
        // "Department 2" is "Auditor", "Administrator" and "Monitor" of "node1"
        // Therefore, jdoe is only "Auditor" of "node1".

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<PersonaResourceRole> query = builder.createQuery(PersonaResourceRole.class);
        Root<PersonaResourceRole> root = query.from(PersonaResourceRole.class);
        query.select(root);
        query.where(
                builder.equal(root.get(PersonaResourceRole_.persona), persona),
                builder.equal(root.get(PersonaResourceRole_.resource), resource)
        );

        List<PersonaResourceRole> results = em.createQuery(query).getResultList();
        if (results.size() == 0) {
            // this means: this persona has no direct roles on the resource, let's check the organizations it belongs to
            List<OrganizationMembership> memberships = membershipService.getMembershipsForPersona(persona);
            List<Organization> organizations = organizationService.getOrganizationsFromMemberships(memberships);

            Set<Role> roles = new HashSet<>();
            for (Organization organization : organizations) {
                // here, we basically filter what are the minimum set of roles a persona has on a resource
                // example:
                // persona "jdoe" is "Auditor" "acme"
                // "acme" is "Monitor" and "Auditor" on "node1"
                // therefore, "jdoe" is only "Auditor" on "node1"

                Set<Role> organizationRolesForResource = getEffectiveRolesForResource(organization, resource);
                Set<Role> effectiveRoles = memberships
                        .stream()
                        // accept only memberships for the current organization
                        .filter(m -> m.getOrganization().equals(organization))
                        // get what are the persona's roles on the organization
                        .map(OrganizationMembership::getRole)
                        // accept only roles that the organization has on the resource
                        .filter(organizationRolesForResource::contains)
                        // the result is: roles that the persona has on the organization, restricted to only the
                        // roles that the organization has on the resource
                        .collect(Collectors.toSet());

                roles.addAll(effectiveRoles);
            }
            return roles;
        }

        Set<Role> roles = new HashSet<>(results.size());
        roles.addAll(results.stream().map(PersonaResourceRole::getRole).collect(Collectors.toSet()));

        return roles;
    }

    @Override
    @Produces
    public Persona getCurrent() {
        // for now, this is sufficient. In a future improvement, we'll have a way to switch users
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
        // TODO: check the permissions, to see if the user indeed has the permissions to impersonate...
        return true;
    }
}
