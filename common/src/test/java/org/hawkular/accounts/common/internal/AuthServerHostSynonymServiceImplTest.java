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
package org.hawkular.accounts.common.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class AuthServerHostSynonymServiceImplTest {
    private AuthServerHostSynonymServiceImpl service;

    @Before
    public void setup() {
        service = new AuthServerHostSynonymServiceImpl();
    }

    @Test
    public void withoutSystemPropertiesParsesLocalIPs() throws SocketException {
        service.determineHostSynonyms();
        Set<InetAddress> ips = service.getLocalIPs();
        assertNotNull("List of local IPs should have been resolved.", ips);
        assertTrue("There should be at least one IP at the list", ips.size() > 0);
    }

    @Test
    public void withSystemPropertiesShouldNotParsesLocalIPs() throws SocketException {
        service = new AuthServerHostSynonymServiceImpl("localhost,0.0.0.0,127.0.0.1", null);
        service.determineHostSynonyms();
        Set<InetAddress> ips = service.getLocalIPs();
        assertNull("The list of IPs should be null", ips);
    }

    @Test
    public void withSystemPropertiesShouldFindSynonym() throws SocketException {
        service = new AuthServerHostSynonymServiceImpl("localhost,0.0.0.0,127.0.0.1", null);
        service.determineHostSynonyms();

        Set<InetAddress> ips = service.getLocalIPs();
        assertNull("The list of IPs should be null", ips);

        assertTrue("Should have found an explicit value from the list", service.isHostSynonym("localhost"));
        assertTrue("Should have found an explicit value from the list", service.isHostSynonym("0.0.0.0"));
        assertTrue("Should have found an explicit value from the list", service.isHostSynonym("127.0.0.1"));
    }

    @Test
    public void withSystemPropertiesShouldNotFindMissingSynonym() throws SocketException {
        service = new AuthServerHostSynonymServiceImpl("localhost,0.0.0.0,127.0.0.1", null);
        service.determineHostSynonyms();

        Set<InetAddress> ips = service.getLocalIPs();
        assertNull("The list of IPs should be null", ips);

        assertFalse("Should not have found an explicit value from the list", service.isHostSynonym("evil.acme.org"));
        assertFalse("Should not have found an explicit value from the list", service.isHostSynonym("8.8.8.8"));
    }

    @Test
    public void shouldDefaultTo_127_0_0_1() throws SocketException {
        service = new AuthServerHostSynonymServiceImpl();
        service.determineHostSynonyms();

        assertTrue("127.0.0.1 should have been found", service.isHostSynonym("127.0.0.1"));
    }

    @Test
    public void boundToAnIp() throws SocketException {
        service = new AuthServerHostSynonymServiceImpl(null, "127.0.0.1");
        service.determineHostSynonyms();

        assertTrue("127.0.0.1 should have been found", service.isHostSynonym("127.0.0.1"));
    }

    @Test
    public void boundToAllLocalIps() throws SocketException {
        service = spy(new AuthServerHostSynonymServiceImpl(null, "0.0.0.0"));
        service.determineHostSynonyms();

        assertTrue("127.0.0.1 should have been found", service.isHostSynonym("127.0.0.1"));
        assertTrue("0.0.0.0 should have been found", service.isHostSynonym("0.0.0.0"));
        assertTrue("localhost should have been found", service.isHostSynonym("localhost"));
    }

    @Test
    public void boundToAHost() throws SocketException {
        service = new AuthServerHostSynonymServiceImpl(null, "localhost");
        service.determineHostSynonyms();

        assertTrue("127.0.0.1 should have been found", service.isHostSynonym("127.0.0.1"));
    }

    @Test
    public void cacheHit() throws SocketException {
        service = spy(new AuthServerHostSynonymServiceImpl(null, "localhost"));
        service.determineHostSynonyms();

        assertTrue("127.0.0.1 should have been found", service.isHostSynonym("127.0.0.1"));
        verify(service, times(1)).checkNewHost(anyObject());

        assertTrue("Cache should have been hit", service.isHostSynonym("127.0.0.1"));
        verify(service, times(1)).checkNewHost(anyObject());

        assertTrue("New entry should be a new check", service.isHostSynonym("localhost"));
        verify(service, times(2)).checkNewHost(anyObject());
    }
}
