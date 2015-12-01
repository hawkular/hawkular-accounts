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

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
class StatusEndpointTest extends BaseSmokeTest {
    @Test
    void checkStatus() {
        def unauthenticatedClient = new RESTClient(baseURI, ContentType.JSON)

        def response = unauthenticatedClient.get(path: '/hawkular/accounts/status')
        assertEquals(200, response.status)
        def implementationVersion = response.data["Implementation-Version"]
        def builtFromSha = response.data["Built-From-Git-SHA1"]

        assertNotNull("Implementation-Version should be present in the response", implementationVersion)
        assertNotNull("Built-From-Git-SHA1 should be present in the response", builtFromSha)
    }
}
