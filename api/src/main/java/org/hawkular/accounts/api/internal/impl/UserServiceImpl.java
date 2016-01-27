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

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hawkular.accounts.api.CurrentUser;
import org.hawkular.accounts.api.UserService;
import org.hawkular.accounts.api.internal.BoundStatements;
import org.hawkular.accounts.api.internal.NamedStatement;
import org.hawkular.accounts.api.model.HawkularUser;
import org.keycloak.KeycloakPrincipal;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Main implementation of the {@link org.hawkular.accounts.api.UserService}. Consumers should get an instance of this
 * via CDI. This class should not be directly instantiated by the consumers.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@PermitAll
public class UserServiceImpl extends BaseServiceImpl<HawkularUser> implements UserService {
    MsgLogger logger = MsgLogger.LOGGER;

    @SuppressWarnings("EjbEnvironmentInspection") @Resource
    SessionContext sessionContext;

    @Inject @NamedStatement(BoundStatements.USER_GET_BY_ID)
    Instance<BoundStatement> stmtGetByIdInstance;

    @Inject @NamedStatement(BoundStatements.USER_CREATE)
    Instance<BoundStatement> stmtCreateInstance;

    @Inject @NamedStatement(BoundStatements.USER_UPDATE)
    Instance<BoundStatement> stmtUpdateInstance;

    @Inject @NamedStatement(BoundStatements.USER_ALL)
    Instance<BoundStatement> stmtAllUsersInstance;

    @Produces @CurrentUser
    @Override
    public HawkularUser getCurrent() {
        Principal p = sessionContext.getCallerPrincipal();
        if (!(p instanceof KeycloakPrincipal)) {
            logger.nonAuthRequestWantsPersona();
            return null;
        }

        KeycloakPrincipal principal = (KeycloakPrincipal) p;
        String id = principal.getName();
        String name = principal.getKeycloakSecurityContext().getToken().getName();
        String email = principal.getKeycloakSecurityContext().getToken().getEmail();
        HawkularUser user = getOrCreateByIdAndName(id, name);

        boolean needsUpdate = false;
        if (!name.equals(user.getName())) {
            logger.settingUsersName(id, name, user.getName());
            user.setName(name);
            needsUpdate = true;
        }
        if (null != email && !email.equals(user.getEmail())) {
            logger.settingUsersEmail(id, email, user.getEmail());
            user.setEmail(email);
            needsUpdate = true;
        }

        if (needsUpdate) {
            return update(user);
        }

        return user;
    }

    @Override
    public HawkularUser getById(String id) {
        return getById(UUID.fromString(id));
    }

    @Override
    public HawkularUser getById(UUID id) {
        return getById(id, stmtGetByIdInstance.get());
    }

    @Override
    public HawkularUser getOrCreateById(String id) {
        HawkularUser user = getById(id);
        if (null == user) {
            logger.creatingUser(id);
            user = create(id, null);
        }

        return user;
    }

    @Override
    public HawkularUser getOrCreateByIdAndName(String id, String name) {
        HawkularUser user = getById(id);
        if (null == user) {
            logger.creatingUserWithName(id, name);
            user = create(id, name);
        }

        return user;
    }

    private HawkularUser create(UUID id, String name) {
        BoundStatement stmtCreate = stmtCreateInstance.get();
        HawkularUser user = new HawkularUser(id, name);
        bindBasicParameters(user, stmtCreate);
        stmtCreate.setString("name", user.getName());
        stmtCreate.setString("email", user.getEmail());
        session.execute(stmtCreate);
        return user;
    }

    private HawkularUser update(HawkularUser user) {
        return update(
                user,
                stmtUpdateInstance.get()
                        .setString("name", user.getName())
                        .setString("email", user.getEmail())
        );
    }

    private HawkularUser create(String id, String name) {
        return create(UUID.fromString(id), name);
    }

    List<HawkularUser> getAll() {
        logger.listingAllUsers();
        return getList(stmtAllUsersInstance.get());
    }

    @Override
    HawkularUser getFromRow(Row row) {
        HawkularUser.Builder builder = new HawkularUser.Builder();
        mapBaseFields(row, builder);
        return builder.name(row.getString("name")).email(row.getString("email")).build();
    }
}
