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

import org.hawkular.accounts.api.OrganizationMembershipService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.OrganizationMembership;
import org.hawkular.accounts.api.model.OrganizationMembership_;
import org.hawkular.accounts.api.model.Persona;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class OrganizationMembershipServiceImpl implements OrganizationMembershipService {
    @Inject
    @HawkularAccounts
    EntityManager em;

    @Override
    public List<OrganizationMembership> getMembershipsForPersona(Persona persona) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<OrganizationMembership> query = builder.createQuery(OrganizationMembership.class);
        Root<OrganizationMembership> root = query.from(OrganizationMembership.class);
        query.select(root);
        query.where(builder.equal(root.get(OrganizationMembership_.member), persona));

        return em.createQuery(query).getResultList();
    }
}
