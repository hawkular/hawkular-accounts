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
    private String cassandraPort;
    private String cassandraNodes;

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Produces @CassandraNodes
    public String getCassandraNodes() {
        if (null == cassandraNodes) {
            cassandraNodes = System.getenv("CASSANDRA_NODES");
            if (null == cassandraNodes || cassandraNodes.isEmpty()) {
                cassandraNodes = "127.0.0.1";
            }
        }
        return cassandraNodes;
    }

    @Produces @CassandraPort
    public String getCassandraPort() {
        if (null == cassandraPort) {
            cassandraPort = System.getenv("CASSANDRA_CQL_PORT");
            if (null == cassandraPort || cassandraPort.isEmpty()) {
                cassandraPort = "9042";
            }
        }
        return cassandraPort;
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

    private void parseRealmConfiguration() {
        JsonReader jsonReader = Json.createReader(new StringReader(getRealmConfiguration()));
        JsonObject configurationJson = jsonReader.readObject();
        JsonObject credentials = configurationJson.getJsonObject("credentials");

        realmName = configurationJson.getString("realm");
        resourceName = configurationJson.getString("resource");
        secret = credentials.getString("secret");

        if (configurationJson.containsKey("auth-server-url-for-backend-requests")) {
            serverUrl = configurationJson.getString("auth-server-url-for-backend-requests");
        } else {
            String authContextPath = "/auth";
            if (configurationJson.containsKey("auth-server-url")) {
                authContextPath = configurationJson.getString("auth-server-url");
            }

            int portOffset = Integer.parseInt(System.getProperty("jboss.socket.binding.port-offset", "0"));
            int defaultPort = Integer.parseInt(System.getProperty("jboss.http.port", "8080"));
            String host = System.getProperty("jboss.bind.address", "127.0.0.1");

            if (authContextPath.toLowerCase().startsWith("http")) {
                serverUrl = authContextPath;
            } else {
                serverUrl = "http://" + host + ":" + (defaultPort+portOffset) + authContextPath;
            }

        }

        realmConfigurationParsed = true;
    }
}
