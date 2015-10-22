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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.security.Identity;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.transaction.UserTransaction;

import org.hawkular.accounts.api.model.HawkularUser;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;

/**
 * @author Juraci Paixão Kröhling
 */
public class UserServiceImplTest extends SessionEnabledTest {
    @Before
    public void addSessionContextToUserService() {
        userService.sessionContext = sessionContext;
    }

    @Test
    public void createUserOnDemandBasedOnCurrentUser() throws IOException {
        int numExistingUsers = userService.getAll().size();
        userService.getCurrent();

        List<HawkularUser> existingUsers = userService.getAll();
        assertEquals("There should 1 persona at the end of the test", numExistingUsers+1, existingUsers.size());
    }

    @Test
    public void createUserOnDemandBasedOnUserId() throws IOException {
        int numExistingUsers = userService.getAll().size();
        userService.getOrCreateById(UUID.randomUUID().toString());

        List<HawkularUser> existingUsers = userService.getAll();
        assertEquals("There should 1 persona at the end of the test", numExistingUsers+1, existingUsers.size());
    }

    @Test
    public void retrieveExistingUserById() throws IOException {
        String id = UUID.randomUUID().toString();
        userService.getOrCreateById(id);
        HawkularUser user = userService.getById(id);
        assertNotNull("User should exist", user);
    }

    @Test
    public void nonExistingUserReturnsNull() {
        HawkularUser user = userService.getById(UUID.randomUUID().toString());
        assertNull("User should not exist", user);
    }

    // added to the bottom of the class, to not pollute the class too much
    // we could actually use a mock framework, but seems too much for this single case...
    // if we need more mock objects, then we'll change this
    SessionContext sessionContext = new SessionContext() {
        @Override
        public Principal getCallerPrincipal() {
            return new KeycloakPrincipal<>(UUID.randomUUID().toString(), new KeycloakSecurityContext() {
                @Override
                public AccessToken getToken() {
                    return new AccessToken() {
                        @Override
                        public String getName() {
                            return "John Doe";
                        }
                    };
                }
            });
        }

        @Override
        public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
            return null;
        }

        @Override
        public EJBObject getEJBObject() throws IllegalStateException {
            return null;
        }

        @Override
        public javax.xml.rpc.handler.MessageContext getMessageContext() throws IllegalStateException {
            return null;
        }

        @Override
        public <T> T getBusinessObject(Class<T> aClass) throws IllegalStateException {
            return null;
        }

        @Override
        public Class getInvokedBusinessInterface() throws IllegalStateException {
            return null;
        }

        @Override
        public boolean wasCancelCalled() throws IllegalStateException {
            return false;
        }

        @Override
        public EJBHome getEJBHome() {
            return null;
        }

        @Override
        public EJBLocalHome getEJBLocalHome() {
            return null;
        }

        @Override
        public Properties getEnvironment() {
            return null;
        }

        @Override
        public Identity getCallerIdentity() {
            return null;
        }

        @Override
        public Map<String, Object> getContextData() {
            return null;
        }

        @Override
        public boolean isCallerInRole(Identity identity) {
            return false;
        }

        @Override
        public boolean isCallerInRole(String s) {
            return false;
        }

        @Override
        public UserTransaction getUserTransaction() throws IllegalStateException {
            return null;
        }

        @Override
        public void setRollbackOnly() throws IllegalStateException {

        }

        @Override
        public boolean getRollbackOnly() throws IllegalStateException {
            return false;
        }

        @Override
        public TimerService getTimerService() throws IllegalStateException {
            return null;
        }

        @Override
        public Object lookup(String s) throws IllegalArgumentException {
            return null;
        }
    };

}
