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

import org.hawkular.accounts.api.OwnerService;
import org.hawkular.accounts.api.internal.adapter.HawkularAccounts;
import org.hawkular.accounts.api.model.Owner;
import org.hawkular.accounts.api.model.Owner_;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Main implementation of the {@link org.hawkular.accounts.api.OwnerService}. Consumers should get an instance of this
 * via CDI. This class should not be directly instantiated by the consumers.
 *
 * @author jpkroehling
 */
@Stateless
@PermitAll
public class OwnerServiceImpl implements OwnerService {

    @Inject
    @HawkularAccounts
    EntityManager em;

    public Owner getById(String id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Owner> query = builder.createQuery(Owner.class);
        Root<Owner> root = query.from(Owner.class);
        query.select(root);
        query.where(builder.equal(root.get(Owner_.id), id));

        List<Owner> results = em.createQuery(query).getResultList();
        if (results.size() == 1) {
            return results.get(0);
        }

        if (results.size() > 1) {
            throw new IllegalStateException("More than one owner found for ID " + id);
        }

        return null;
    }
}
