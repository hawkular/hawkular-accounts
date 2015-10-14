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

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.PersonaService;
import org.hawkular.accounts.api.ResourceService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.OrganizationMembership_;
import org.hawkular.accounts.api.model.Persona;
import org.hawkular.accounts.api.model.Resource;
import org.hawkular.accounts.api.model.Role;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class OrganizationMembershipServiceImpl implements OrganizationMembershipService {
    @Inject
    @HawkularAccounts
    EntityManager em;

    @Inject
    ResourceService resourceService;

    @Inject
    PersonaService personaService;

    @Override
    public OrganizationMembership create(Organization organization, Persona persona, Role role) {
        OrganizationMembership membership = new OrganizationMembership(organization, persona, role);
        em.persist(membership);

        // for permission checking
        Resource resource = resourceService.get(organization.getId());
        resourceService.addRoleToPersona(resource, persona, role);
        return membership;
    }

    @Override
    public List<OrganizationMembership> getMembershipsForPersona(Persona persona) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<OrganizationMembership> query = builder.createQuery(OrganizationMembership.class);
        Root<OrganizationMembership> root = query.from(OrganizationMembership.class);
        query.select(root);
        query.where(builder.equal(root.get(OrganizationMembership_.member), persona));

        return em.createQuery(query).getResultList();
    }

    @Override
    public List<OrganizationMembership> getMembershipsForOrganization(Organization organization) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<OrganizationMembership> query = builder.createQuery(OrganizationMembership.class);
        Root<OrganizationMembership> root = query.from(OrganizationMembership.class);
        query.select(root);
        query.where(builder.equal(root.get(OrganizationMembership_.organization), organization));

        return em.createQuery(query).getResultList();
    }

    @Override public List<OrganizationMembership> getPersonaMembershipsForOrganization(Persona persona,
                                                                                       Organization organization) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<OrganizationMembership> query = builder.createQuery(OrganizationMembership.class);
        Root<OrganizationMembership> root = query.from(OrganizationMembership.class);
        query.select(root);
        query.where(
                builder.equal(root.get(OrganizationMembership_.organization), organization),
                builder.equal(root.get(OrganizationMembership_.member), persona)
        );

        return em.createQuery(query).getResultList();
    }

    @Override public OrganizationMembership getMembershipById(String id) {
        if (null == id) {
            throw new IllegalArgumentException("The given membership ID is invalid (null).");
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<OrganizationMembership> query = builder.createQuery(OrganizationMembership.class);
        Root<OrganizationMembership> root = query.from(OrganizationMembership.class);
        query.select(root);
        query.where(builder.equal(root.get(OrganizationMembership_.id), id));

        List<OrganizationMembership> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            return results.get(0);
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one membership found for ID " + id);
        }

        return null;
    }

    @Override
    public OrganizationMembership changeRole(OrganizationMembership membership, Role role) {
        membership.setRole(role);
        em.persist(membership);

        // the code above was for "organization" data. the code below is for RBAC.
        // for now, we allow only one role for each organization, so, revoke all current roles and add the given role
        Persona persona = personaService.get(membership.getMember().getId());
        Resource resource = resourceService.get(membership.getOrganization().getId());
        resourceService.revokeAllForPersona(resource, persona);
        resourceService.addRoleToPersona(resource, persona, role);

        return membership;
    }
}
