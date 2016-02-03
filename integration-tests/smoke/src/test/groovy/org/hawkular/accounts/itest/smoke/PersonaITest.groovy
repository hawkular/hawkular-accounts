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
package org.hawkular.accounts.itest.smoke

import groovyx.net.http.HttpResponseException
import org.jboss.arquillian.junit.Arquillian
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
class PersonaITest extends BaseSmokeTest {

    @Test
    void canGetCurrentPersona() {
        // just a sanity test before we start for real
        def response = client.get(path: "/hawkular/accounts/personas/current")
        assertEquals(200, response.status)
        assertNotNull(response.data.id)
    }

    @Test
    void canGetPreferredPersona() {
        def orgName = UUID.randomUUID().toString()
        def response = client.post(
                path: '/hawkular/accounts/organizations',
                body: [name: orgName]
        )

        assertEquals(200, response.status)
        assertNotNull(response.data.id)
        assertEquals('The company name should have been persisted', orgName, response.data.name)

        def preferredPersonaId = response.data.id;

        response = client.get(path: "/hawkular/accounts/personas/${preferredPersonaId}")
        assertEquals(200, response.status)
        assertEquals(response.data.id, preferredPersonaId)
        assertNotNull(response.data.id)
    }

    @Test
    void preferredPersonaCannotBeFound() {
        // just a sanity test before we start for real
        try {
            client.get(path: "/hawkular/accounts/personas/some-non-sense")
        } catch (HttpResponseException exception) {
            assertEquals(404, exception.response.status)
        }
    }

    @Test
    void preferredPersonaCannotBeFoundWithUUID() {
        // just a sanity test before we start for real
        try {
            client.get(path: "/hawkular/accounts/personas/${UUID.randomUUID().toString()}")
        } catch (HttpResponseException exception) {
            assertEquals(404, exception.response.status)
        }
    }

    @Test
    void canAuthenticateWithAsPersona() {
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
        response = client.get(path: "/hawkular/accounts/personas/current")
        assertEquals(200, response.status)
        assertEquals("Should be recognized as Acme, Inc", organizationId, response.data.id)
    }

    @Test
    void unknownPersona() {
        def response = client.get(path: "/hawkular/accounts/personas/current")
        assertEquals(200, response.status)

        try {
            client.defaultRequestHeaders."Hawkular-Persona" = UUID.randomUUID().toString()
            client.get(path: "/hawkular/accounts/personas/current")
        } catch (HttpResponseException exception) {
            assertEquals(500, exception.response.status)
        } finally {
            client.defaultRequestHeaders.remove("Hawkular-Persona")
        }
    }
}
