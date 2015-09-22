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

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletContext;

/**
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class ApplicationResources {
    private static final String REALM_CONFIG_KEY = "org.keycloak.json.adapterConfig";
    private String realmConfiguration = null;
    private ServletContext servletContext;

    private boolean realmConfigurationParsed = false;

    private String realmName;
    private String serverUrl;
    private String resourceName;
    private String secret;
    private Set<String> hostSynonyms;

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Produces @RealmConfiguration
    public String getRealmConfiguration() {
        if (null == realmConfiguration) {
            realmConfiguration = servletContext.getInitParameter(REALM_CONFIG_KEY);
        }
        return realmConfiguration;
    }

    @Produces @RealmName
    public String getRealmName() {
        if (!realmConfigurationParsed) {
            parseRealmConfiguration();
        }
        return realmName;
    }

    @Produces @AuthServerUrl
    public String getServerUrl() {
        if (!realmConfigurationParsed) {
            parseRealmConfiguration();
        }
        return serverUrl;
    }

    @Produces @RealmResourceName
    public String getResourceName() {
        if (!realmConfigurationParsed) {
            parseRealmConfiguration();
        }
        return resourceName;
    }

    @Produces @RealmResourceSecret
    public String getResourceNameSecret() {
        if (!realmConfigurationParsed) {
            parseRealmConfiguration();
        }
        return secret;
    }

    @Produces @HostSynonyms
    public Set<String> getHostSynonyms() {
        if (hostSynonyms == null) {
            String synonyms = System.getProperty("org.hawkular.accounts.host.synonyms", "localhost,127.0.0.1,0.0.0.0");
            hostSynonyms = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(synonyms.split(","))));
        }

        return hostSynonyms;
    }

    private void parseRealmConfiguration() {
        JsonReader jsonReader = Json.createReader(new StringReader(getRealmConfiguration()));
        JsonObject configurationJson = jsonReader.readObject();
        JsonObject credentials = configurationJson.getJsonObject("credentials");

        realmName = configurationJson.getString("realm");
        serverUrl = configurationJson.getString("auth-server-url-for-backend-requests");
        resourceName = configurationJson.getString("resource");
        secret = credentials.getString("secret");

        realmConfigurationParsed = true;
    }
}
