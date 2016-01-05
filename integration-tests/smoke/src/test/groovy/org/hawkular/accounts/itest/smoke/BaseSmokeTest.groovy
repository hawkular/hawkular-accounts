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

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.junit.BeforeClass

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 * @author Juraci Paixão Kröhling
 */
class BaseSmokeTest {
    protected static final String testUser = 'jdoe'
    protected static final String testPasword = 'password'

    protected static final String baseURI
    static {
        String host = System.getProperty('hawkular.bind.address') ?: 'localhost'
        if ('0.0.0.0'.equals(host)) {
            host = 'localhost'
        }
        int portOffset = Integer.parseInt(System.getProperty('hawkular.port.offset') ?: '0')
        int httpPort = portOffset + 8080
        baseURI = "http://${host}:${httpPort}"
    }
    static RESTClient client

    @BeforeClass
    static void initClient() {
        client = new RESTClient(baseURI, ContentType.JSON)

        /* http://en.wikipedia.org/wiki/Basic_access_authentication#Client_side :
         * The Authorization header is constructed as follows:
         *  * Username and password are combined into a string "username:password"
         *  * The resulting string is then encoded using the RFC2045-MIME variant of Base64,
         *    except not limited to 76 char/line[9]
         *  * The authorization method and a space i.e. "Basic " is then put before the encoded string.
         */
        String encodedCredentials = Base64.getEncoder().encodeToString("$testUser:$testPasword".getBytes('utf-8'))
        client.defaultRequestHeaders.Authorization = "Basic ${encodedCredentials}"
        client.defaultRequestHeaders.Accept = ContentType.JSON
    }

    static RESTClient getClientForUsernameAndPassword(String username, String password) {
        def toEncode = "${username}:${password}"
        def client = new RESTClient(baseURI, ContentType.JSON)
        String encodedCredentials = Base64.getEncoder().encodeToString(toEncode.getBytes('utf-8'))
        client.defaultRequestHeaders.Authorization = "Basic ${encodedCredentials}"
        return client
    }

    static RESTClient getJdoeClientForPersona(String personaId) {
        return getClientForPersona(testUser, testPasword, personaId)
    }

    static RESTClient getClientForPersona(String username, String password, String personaId) {
        def toEncode = "${username}:${password}"
        def client = new RESTClient(baseURI, ContentType.JSON)
        String encodedCredentials = Base64.getEncoder().encodeToString(toEncode.getBytes('utf-8'))
        client.defaultRequestHeaders.Authorization = "Basic ${encodedCredentials}"
        client.defaultRequestHeaders.'Hawkular-Persona' = personaId
        return client
    }

    static Object doCreatePrivateOrganization(String orgName) {
        def response = doCreateOrganizationWithVisibility(orgName, 'PRIVATE')
        assertEquals('PRIVATE', response.data.visibility)
        return response
    }

    static Object doCreateApplyOrganization(String orgName) {
        def response = doCreateOrganizationWithVisibility(orgName, 'APPLY')
        assertEquals('APPLY', response.data.visibility)
        return response
    }

    static Object doCreateOrganizationWithVisibility(String orgName, String visibility) {
        def response = client.post(
                path: '/hawkular/accounts/organizations',
                body: [name: orgName, visibility: visibility]
        )
        assertEquals(200, response.status)
        assertNotNull(response.data.id)
        return response
    }

    static Object doCreateOrganization(String orgName) {
        def response = client.post(
                path: '/hawkular/accounts/organizations',
                body: [name: orgName]
        )
        assertEquals(200, response.status)
        assertNotNull(response.data.id)
        return response
    }
}
