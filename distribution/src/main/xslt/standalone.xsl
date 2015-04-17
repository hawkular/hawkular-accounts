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

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" xalan:indent-amount="4" standalone="no"/>
  <xsl:strip-space elements="*"/>

  <!-- Add our data source -->
  <xsl:template match="node()[name(.)='datasources']">
    <xsl:copy>
      <xsl:apply-templates select="node()[name(.)='datasource']"/>
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
    <cache-container name="hawkular-accounts" default-cache="role-cache">
      <local-cache name="role-cache"/>
      <local-cache name="operation-cache"/>
    </cache-container>
  </xsl:template>

  <!-- Add a secure-deployment to the keycloak subsystem -->
  <xsl:template match="node()[name(.)='auth-server']">
    <xsl:copy>
      <xsl:copy-of select="node()|@*"/>
    </xsl:copy>
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
  </xsl:template>

  <!-- Everything else remains the same -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>