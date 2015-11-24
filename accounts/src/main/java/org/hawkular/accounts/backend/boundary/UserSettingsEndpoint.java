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
package org.hawkular.accounts.backend.boundary;

import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.UserSettingsService;
import org.hawkular.accounts.api.model.HawkularUser;
import org.hawkular.accounts.api.model.UserSettings;

/**
 * @author Juraci Paixão Kröhling
 */
@Path("/settings")
@PermitAll
@Stateless
public class UserSettingsEndpoint {
    @Inject
    UserSettingsService service;

    @Inject @CurrentUser
    Instance<HawkularUser> userInstance;

    @GET
    public Response getByUser() {
        HawkularUser user = userInstance.get();
        UserSettings settings = service.getOrCreateByUser(user);
        return Response.ok(settings.getProperties()).build();
    }

    @PUT
    public Response storeKey(Map<String, String> properties) {
        HawkularUser user = userInstance.get();
        properties.forEach((key, value) -> service.store(user, key, value));
        UserSettings settings = service.getByUser(user);
        return Response.ok(settings.getProperties()).build();
    }
}
