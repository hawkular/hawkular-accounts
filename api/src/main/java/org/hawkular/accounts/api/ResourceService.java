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

import org.hawkular.accounts.api.model.Owner;
import org.hawkular.accounts.api.model.Resource;

/**
 * Manages {@link org.hawkular.accounts.api.model.Resource}. A Resource can be anything that is meant to be protected
 * by the consumer modules. For instance, a Resource could be an Alert, an Inventory item or a Metric.
 *
 * Implementations of this interface should conform with CDI rules and be injectable into managed beans. For
 * consumers, it means that a concrete implementation of this interface can be injected via {@link javax.inject.Inject}
 *
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
public interface ResourceService {
    /**
     * Retrieves a {@link Resource} based on its ID.
     *
     * @param id the resource's ID
     * @return the existing {@link Resource} or null if the resource doesn't exists.
     */
    Resource get(String id);

    /**
     * Creates a {@link Resource} based on its ID, owned by the specified {@link org.hawkular.accounts.api.model.Owner}
     *
     * @param id    the ID to be assigned to this resource or null for a new UUID
     * @param owner a valid owner for this resource
     * @return the newly created {@link Resource}
     */
    Resource create(String id, Owner owner);

    /**
     * Creates a new sub resource, based on a given ID and owning resource
     *
     * @param id    the ID to be assigned to this resource or null for a new UUID
     * @param parent a valid resource to serve as the parent of this sub resource
     * @return the newly created {@link Resource}
     */
    Resource create(String id, Resource parent);

    /**
     * Creates a new sub resource, based on a given ID, parent and owned by the specified
     * {@link org.hawkular.accounts.api.model.Owner}
     *
     * @param id     the ID to be assigned to this resource or null for a new UUID
     * @param parent the resource's parent or null if the owner is provided
     * @param owner  the resource's owner or null if the parent is provided
     * @return the newly created {@link Resource}
     */
    Resource create(String id, Resource parent, Owner owner);

    /**
     * Removes a {@link Resource} based on its ID.
     *
     * @param id    the resource's ID
     */
    void delete(String id);
}
