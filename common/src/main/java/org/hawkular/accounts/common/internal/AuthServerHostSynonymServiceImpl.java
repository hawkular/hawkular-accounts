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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.hawkular.accounts.common.AuthServerHostSynonymService;

/**
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class AuthServerHostSynonymServiceImpl implements AuthServerHostSynonymService {
    private MsgLogger logger = MsgLogger.LOGGER;
    private static final String WILDCARD_ADDRESS = "0.0.0.0";
    private static final String DEFAULT_ADDRESS = "127.0.0.1";

    /**
     * For this service, we have two algorithms: the first is just a match between the specified system property
     * and the value that is to be checked. The second is here called as 'best effort', and
     * we try our best to tell if a given host is a synonym for the local host.
     */
    private boolean bestEffort = true;

    private Set<String> hostSynonyms;
    private Map<String, Boolean> hostnameCache;
    private Set<InetAddress> localIPs;
    private String synonyms;
    private String boundToAddress;

    public AuthServerHostSynonymServiceImpl() {
        synonyms = System.getProperty("org.hawkular.accounts.auth.host.synonyms");
        boundToAddress = System.getProperty("jboss.bind.address");
    }

    public AuthServerHostSynonymServiceImpl(String synonyms, String boundToAddress) {
        this.synonyms = synonyms;
        this.boundToAddress = boundToAddress;
    }

    @PostConstruct
    void determineHostSynonyms() {
        if (synonyms != null && !synonyms.isEmpty()) {
            hostSynonyms = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(synonyms.split(","))));
            bestEffort = false;
            logger.listOfSynonymsProvided();
            return;
        }

        // we have not found the system property, proceed with trying to get the local IPs
        localIPs = new HashSet<>();
        hostnameCache = new HashMap<>();

        // if we are not bound to a specific address, then we are implicitly bound to 127.0.0.1
        if (boundToAddress == null || boundToAddress.isEmpty()) {
            boundToAddress = DEFAULT_ADDRESS;
        }

        // if we are bound to 0.0.0.0, then we are bound to all local addresses (not 100% true, but true for our case)
        if (WILDCARD_ADDRESS.equals(boundToAddress)) {
            logger.allLocalAddressesForSynonyms();
            determineLocalAddresses();
            try {
                localIPs.add(InetAddress.getByName(WILDCARD_ADDRESS));
            } catch (UnknownHostException e) {
                // should *not* happen, but if it does, then let's just ignore... this is a "plus"
                logger.cannotDetermineIPForWildcardHost(WILDCARD_ADDRESS);
            }
            return;
        }

        // at this point, we are either bound to a specific address, or to a hostname
        // if we are bound to a hostname, let's try to find all IPs for it, otherwise, we just add the IP to the list
        InetAddress[] addressForHost;
        try {
            addressForHost = InetAddress.getAllByName(boundToAddress);
        } catch (UnknownHostException e) {
            // how can that be? we are bound to a host which is not known to us? it happens, but should be very rare
            logger.cannotDetermineIPForHost(WILDCARD_ADDRESS, e);
            return;
        }

        Collections.addAll(localIPs, addressForHost);
    }

    @Override
    public boolean isHostSynonym(String host) {
        if (!bestEffort) {
            return hostSynonyms.contains(host);
        }

        // first, check the cache
        if (hostnameCache.containsKey(host)) {
            return hostnameCache.get(host);
        }

        return checkNewHost(host);
    }

    boolean checkNewHost(String host) {
        // not in the cache, so, proceed
        InetAddress[] addressForHost;
        try {
            addressForHost = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            // no, this host is not known at all, it can't be us...
            return false;
        }

        boolean found = false;
        for (InetAddress address : addressForHost) {
            if (localIPs.contains(address)) {
                found = true;
            }
        }

        hostnameCache.put(host, found);
        return found;
    }

    void determineLocalAddresses() {
        // Networking is a complex matter, and I'm not even trying to get this algorithm into a state where it will
        // be correct most of the times, rather, I'm trying to provide a good out-of-the-box experience.
        // For production environments, we should really try to document somewhere that admins *should* set the
        // system property. The reason is: on production machines, we might or might not want to assume that some
        // NICs and/or virtual interfaces are synonyms. One situation where we *don't* want is for bridged interfaces
        // (like docker ones), which usually refers to a container, not the host. Other situations that we don't even
        // try to handle here: firewalls and frontend-proxies. For instance, a company might have a load balancer or
        // frontend-proxy with a public IP address and hostname, but all we (backend) see is a list of private
        // addresses.
        Enumeration<NetworkInterface> networkInterfaceEnumeration = getNetworkInterfaces();

        while (networkInterfaceEnumeration.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
            Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
            while (addressEnumeration.hasMoreElements()) {
                InetAddress address = addressEnumeration.nextElement();
                localIPs.add(address);
            }
        }
    }

    Set<InetAddress> getLocalIPs() {
        if (null == localIPs) {
            return null;
        }
        return Collections.unmodifiableSet(localIPs);
    }

    Enumeration<NetworkInterface> getNetworkInterfaces() {
        try {
            return NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            // uh-oh, we are in trouble... we don't have the system property and we are bound to "all" addresses
            // we don't have much choice other than to re-throw it as a runtime exception
            logger.cannotDetermineLocalIPs(e);
            throw new RuntimeException(e);
        }
    }

}
