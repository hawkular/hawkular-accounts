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
package org.hawkular.accounts.itest.smoke

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.jboss.arquillian.junit.Arquillian
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

@RunWith(Arquillian.class)
class SecretStoreITest extends BaseSmokeTest {

    @Test
    void canGetCurrentPersona() {
        // just a sanity test before we start for real
        def response = client.get(path: "/hawkular/accounts/personas/current")
        assertEquals(200, response.status)
        assertNotNull(response.data.id)
        assertTrue(true)
    }

    @Test
    void createTokenBasedOnCurrentPersonaCredentials() {
        def response = client.post(path: "/hawkular/secret-store/v1/tokens/create")
        assertEquals(200, response.status)
        assertNotNull(response.data.key)
        assertNotNull(response.data.secret)
    }

    @Test
    void canAuthenticateWithTokenAsJdoe() {
        def response = client.get(path: "/hawkular/accounts/personas/current")
        assertEquals(200, response.status)
        def jdoeId = response.data.id

        response = client.post(path: "/hawkular/secret-store/v1/tokens/create")
        assertEquals(200, response.status)

        def token = response.data.key
        def secret = response.data.secret
        def toEncode = "${token}:${secret}"

        def clientWithTokenAuth = new RESTClient(baseURI, ContentType.JSON)
        String encodedCredentials = Base64.getEncoder().encodeToString(toEncode.getBytes("utf-8"))
        clientWithTokenAuth.defaultRequestHeaders.Authorization = "Basic "+ encodedCredentials

        response = clientWithTokenAuth.get(path: "/hawkular/accounts/personas/current")
        assertEquals("With token/secret should be recognized as jdoe", jdoeId, response.data.id)
    }

    @Test
    void canAuthenticateWithTokenAsPersona() {
        def response = client.get(path: "/hawkular/accounts/personas/current")
        assertEquals(200, response.status)
        def jdoeId = response.data.id

        response = client.post(
                path: '/hawkular/accounts/organizations',
                body: [name: UUID.randomUUID().toString()]
        )
        assertEquals(200, response.status)
        def organizationId = response.data.id

        client.defaultRequestHeaders."Hawkular-Persona" = organizationId
        response = client.post(path: "/hawkular/secret-store/v1/tokens/create")
        assertEquals(200, response.status)
        client.defaultRequestHeaders.remove("Hawkular-Persona")

        def token = response.data.key
        def secret = response.data.secret
        def toEncode = "${token}:${secret}"

        def clientWithTokenAuth = new RESTClient(baseURI, ContentType.JSON)
        String encodedCredentials = Base64.getEncoder().encodeToString(toEncode.getBytes("utf-8"))
        clientWithTokenAuth.defaultRequestHeaders.Authorization = "Basic "+ encodedCredentials

        response = clientWithTokenAuth.get(path: "/hawkular/accounts/personas/current")
        assertEquals("With token/secret should be recognized as Acme, Inc", organizationId, response.data.id)
    }
}