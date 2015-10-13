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
package org.hawkular.accounts.common;

/**
 * @author Juraci Paixão Kröhling
 */
public interface AuthServerHostSynonymService {
    /**
     * Determines if the given host is a synonym for any of the hosts that our auth server runs on. The rules are:
     * <ul>
     *     <li>
     *         If the system property org.hawkular.accounts.auth.host.synonyms is specified, then the host is matched
     *         against this list. This list should include all IPs and hostnames that are considered synonyms.
     *     </li>
     *     <li>
     *         If the system property is not set, we assume that the auth server is running on the same
     *         application server instance as Hawkular. We then build a list of local IPs. If the parameter
     *         {@param host} is an IP address, it's matched with this list. If it's a host, then the host is resolved
     *         to an IP, which is then matched with the list. The list of IPs is derived from the property
     *         jboss.bind.address as follows:
     *         <ul>
     *             <li>If it's not defined, then 127.0.0.1 is assumed (based on Wildfly's defaults)</li>
     *             <li>If it's 0.0.0.0, then all available NICs are queried and its IPs are added to a cache</li>
     *             <li>If it's not an IP, then the hostname is resolved into an IP</li>
     *             <li>If it's an IP, it's added as the single entry to the list.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param host    the host to check if it's a synonym for the local host.
     * @return  whether or not the requested host is a synonym for the auth server being used by this instance
     */
    boolean isHostSynonym(String host);
}
