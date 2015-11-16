<!--

    Copyright 2015 Red Hat, Inc. and/or its affiliates
    and other contributors as indicated by the @author tags.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                version="2.0"
                exclude-result-prefixes="xalan j">

  <xsl:param name="config"/>
  <xsl:param name="uuid.hawkular.accounts.backend"/>
  <xsl:param name="uuid.hawkular.ui"/>

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" xalan:indent-amount="4" standalone="no"/>
  <xsl:strip-space elements="*"/>

  <!-- add system properties -->
  <xsl:template name="system-properties">
    <system-properties>
      <property>
        <xsl:attribute name="name">keycloak.import</xsl:attribute>
        <xsl:attribute name="value">&#36;{jboss.home.dir}/standalone/configuration/hawkular-realm.json</xsl:attribute>
      </property>
      <property>
        <xsl:attribute name="name">hawkular.events.listener.rest.endpoint</xsl:attribute>
        <xsl:attribute name="value">http://localhost:8080/hawkular-accounts-events-backend/events</xsl:attribute>
      </property>
      <property>
        <xsl:attribute name="name">hawkular.backend</xsl:attribute>
        <xsl:attribute name="value">&#36;{hawkular.backend:embedded_cassandra}</xsl:attribute>
      </property>
    </system-properties>
  </xsl:template>

  <xsl:template match="node()[name(.)='extensions']">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
      <extension module="org.wildfly.extension.messaging-activemq"/>
      <extension module="org.keycloak.keycloak-adapter-subsystem"/>
      <extension module="org.keycloak.keycloak-server-subsystem"/>
    </xsl:copy>
    <xsl:call-template name="system-properties"/>
  </xsl:template>

  <!-- Add our data source -->
  <xsl:template match="node()[name(.)='datasources']">
    <xsl:copy>
      <xsl:apply-templates select="node()[name(.)='datasource']"/>
      <datasource jndi-name="java:jboss/datasources/KeycloakDS" pool-name="KeycloakDS" enabled="true" use-java-context="true">
        <connection-url>jdbc:h2:${jboss.server.data.dir}${/}h2${/}keycloak;AUTO_SERVER=TRUE</connection-url>
        <driver>h2</driver>
        <security>
          <user-name>sa</user-name>
          <password>sa</password>
        </security>
      </datasource>
      <datasource jndi-name="java:jboss/datasources/HawkularDS" pool-name="HawkularDS" enabled="true" use-java-context="true">
        <connection-url>jdbc:h2:${jboss.server.data.dir}${/}h2${/}hawkular;AUTO_SERVER=TRUE</connection-url>
        <driver>h2</driver>
        <security>
          <user-name>sa</user-name>
        </security>
      </datasource>
      <xsl:apply-templates select="node()[name(.)='drivers']"/>
    </xsl:copy>
  </xsl:template>

  <!-- Add a cache for Hawkular Accounts -->
  <xsl:template match="node()[name(.)='cache-container'][1]">
    <xsl:copy>
      <xsl:copy-of select="node()|@*"/>
    </xsl:copy>
    <cache-container name="keycloak" jndi-name="infinispan/Keycloak">
      <local-cache name="realms"/>
      <local-cache name="users"/>
      <local-cache name="sessions"/>
      <local-cache name="loginFailures"/>
    </cache-container>
    <cache-container name="hawkular-accounts" default-cache="role-cache">
      <local-cache name="role-cache"/>
      <local-cache name="operation-cache"/>
    </cache-container>
    <cache-container name="hawkular-accounts-websocket" default-cache="session-cache">
      <local-cache name="session-cache"/>
    </cache-container>
  </xsl:template>

  <!-- Add MDB support for Hawkular Accounts -->
  <xsl:template match="node()[name(.)='session-bean'][1]">
    <xsl:copy>
      <xsl:copy-of select="node()|@*"/>
    </xsl:copy>
    <mdb>
      <resource-adapter-ref resource-adapter-name="activemq-ra.rar"/>
      <bean-instance-pool-ref pool-name="mdb-strict-max-pool"/>
    </mdb>
  </xsl:template>

  <xsl:template match="node()[name(.)='profile']">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
      <subsystem xmlns="urn:jboss:domain:keycloak-server:1.1">
        <web-context>auth</web-context>
      </subsystem>
      <subsystem xmlns="urn:jboss:domain:keycloak:1.1">
        <realm name="hawkular">
          <auth-server-url>http://localhost:8080/auth</auth-server-url>
          <ssl-required>none</ssl-required>
        </realm>
        <secure-deployment name="hawkular-accounts.war">
          <realm>hawkular</realm>
          <resource>hawkular-accounts-backend</resource>
          <use-resource-role-mappings>true</use-resource-role-mappings>
          <enable-cors>true</enable-cors>
          <enable-basic-auth>true</enable-basic-auth>
          <credential name="secret"><xsl:value-of select="$uuid.hawkular.accounts.backend" /></credential>
        </secure-deployment>
        <secure-deployment name="hawkular-accounts-sample-ui.war">
          <realm>hawkular</realm>
          <resource>hawkular-ui</resource>
          <enable-cors>true</enable-cors>
          <credential name="secret"><xsl:value-of select="$uuid.hawkular.ui" /></credential>
        </secure-deployment>
        <secure-deployment name="hawkular-accounts-secret-store.war">
          <realm>hawkular</realm>
          <resource>hawkular-accounts-backend</resource>
          <use-resource-role-mappings>true</use-resource-role-mappings>
          <enable-cors>true</enable-cors>
          <enable-basic-auth>true</enable-basic-auth>
          <credential name="secret"><xsl:value-of select="$uuid.hawkular.accounts.backend" /></credential>
        </secure-deployment>
        <secure-deployment name="hawkular-accounts-sample.war">
          <realm>hawkular</realm>
          <resource>hawkular-accounts-backend</resource>
          <use-resource-role-mappings>true</use-resource-role-mappings>
          <enable-cors>true</enable-cors>
          <enable-basic-auth>true</enable-basic-auth>
          <credential name="secret"><xsl:value-of select="$uuid.hawkular.accounts.backend" /></credential>
        </secure-deployment>
        <secure-deployment name="hawkular-accounts-sample-websocket-backend.war">
          <realm>hawkular</realm>
          <resource>hawkular-accounts-backend</resource>
          <use-resource-role-mappings>true</use-resource-role-mappings>
          <enable-cors>true</enable-cors>
          <enable-basic-auth>true</enable-basic-auth>
          <credential name="secret"><xsl:value-of select="$uuid.hawkular.accounts.backend" /></credential>
        </secure-deployment>
        <secure-deployment name="hawkular-accounts-sample-websocket-secured.war">
          <realm>hawkular</realm>
          <resource>hawkular-accounts-backend</resource>
          <use-resource-role-mappings>true</use-resource-role-mappings>
          <enable-cors>true</enable-cors>
          <enable-basic-auth>true</enable-basic-auth>
          <credential name="secret"><xsl:value-of select="$uuid.hawkular.accounts.backend" /></credential>
        </secure-deployment>
      </subsystem>
      <subsystem xmlns="urn:jboss:domain:messaging-activemq:1.0">
        <server name="default">
          <security-setting name="#">
            <role name="guest" send="true" consume="true" create-non-durable-queue="true" delete-non-durable-queue="true"/>
          </security-setting>
          <address-setting name="#" dead-letter-address="jms.queue.DLQ" expiry-address="jms.queue.ExpiryQueue" max-size-bytes="10485760" page-size-bytes="2097152" message-counter-history-day-limit="10"/>
          <http-connector name="http-connector" socket-binding="http" endpoint="http-acceptor"/>
          <http-connector name="http-connector-throughput" socket-binding="http" endpoint="http-acceptor-throughput">
            <param name="batch-delay" value="50"/>
          </http-connector>
          <in-vm-connector name="in-vm" server-id="0"/>
          <http-acceptor name="http-acceptor" http-listener="default"/>
          <http-acceptor name="http-acceptor-throughput" http-listener="default">
            <param name="batch-delay" value="50"/>
            <param name="direct-deliver" value="false"/>
          </http-acceptor>
          <in-vm-acceptor name="in-vm" server-id="0"/>
          <jms-queue name="ExpiryQueue" entries="java:/jms/queue/ExpiryQueue"/>
          <jms-queue name="DLQ" entries="java:/jms/queue/DLQ"/>
          <jms-topic name="HawkularAccountsEvents" entries="java:/topic/HawkularAccountsEvents"/>
          <connection-factory name="InVmConnectionFactory" connectors="in-vm" entries="java:/ConnectionFactory"/>
          <connection-factory name="RemoteConnectionFactory" connectors="http-connector" entries="java:jboss/exported/jms/RemoteConnectionFactory"/>
          <pooled-connection-factory name="activemq-ra" transaction="xa" connectors="in-vm" entries="java:/JmsXA java:/HawkularBusConnectionFactory java:jboss/DefaultJMSConnectionFactory"/>
        </server>
      </subsystem>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()[name(.)='security-domains']">
    <xsl:copy>
      <xsl:apply-templates select="node()[name(.)='security-domain']"/>
      <security-domain name="keycloak">
        <authentication>
          <login-module code="org.keycloak.adapters.jboss.KeycloakLoginModule" flag="required"/>
        </authentication>
      </security-domain>
      <security-domain name="sp" cache-type="default">
        <authentication>
          <login-module code="org.picketlink.identity.federation.bindings.wildfly.SAML2LoginModule" flag="required"/>
        </authentication>
      </security-domain>
    </xsl:copy>
  </xsl:template>

  <!-- Everything else remains the same -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
