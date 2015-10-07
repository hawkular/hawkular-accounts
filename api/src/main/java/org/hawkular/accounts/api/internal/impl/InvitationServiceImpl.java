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

import org.hawkular.accounts.api.InvitationService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.Invitation;
import org.hawkular.accounts.api.model.Invitation_;
import org.hawkular.accounts.api.model.Organization;
import org.hawkular.accounts.api.model.OrganizationMembership;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class InvitationServiceImpl implements InvitationService {
    @Inject
    @HawkularAccounts
    EntityManager em;

    @Override
    public Invitation getByToken(String token) {
        if (null == token) {
            throw new IllegalArgumentException("The given Invitation Token is invalid (null).");
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Invitation> query = builder.createQuery(Invitation.class);
        Root<Invitation> root = query.from(Invitation.class);
        query.select(root);
        query.where(builder.equal(root.get(Invitation_.token), token));

        List<Invitation> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            return results.get(0);
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one invitation found for token " + token);
        }

        return null;
    }

    @Override
    public Invitation get(String id) {
        if (null == id) {
            throw new IllegalArgumentException("The given Invitation ID is invalid (null).");
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Invitation> query = builder.createQuery(Invitation.class);
        Root<Invitation> root = query.from(Invitation.class);
        query.select(root);
        query.where(builder.equal(root.get(Invitation_.id), id));

        List<Invitation> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            return results.get(0);
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one invitation found for ID " + id);
        }

        return null;
    }

    @Override
    public List<Invitation> getPendingInvitationsForOrganization(Organization organization) {
        if (null == organization) {
            throw new IllegalArgumentException("The given Organization is invalid (null).");
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Invitation> query = builder.createQuery(Invitation.class);
        Root<Invitation> root = query.from(Invitation.class);
        query.select(root);
        query.where(
                builder.equal(root.get(Invitation_.organization), organization),
                builder.isNull(root.get(Invitation_.acceptedAt))
        );

        return em.createQuery(query).getResultList();
    }

    @Override
    public Invitation create(Invitation invitation) {
        em.persist(invitation);
        return invitation;
    }

    @Override
    public Invitation accept(Invitation invitation, HawkularUser user) {
        OrganizationMembership membership = new OrganizationMembership(
                invitation.getOrganization(),
                user,
                invitation.getRole());

        invitation.setAccepted();
        invitation.setAcceptedBy(user);
        em.persist(invitation);
        em.persist(membership);

        return invitation;
    }


}
