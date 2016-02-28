<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xalan" version="2.0" exclude-result-prefixes="xalan">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" xalan:indent-amount="4" standalone="no" />
  <xsl:strip-space elements="*" />

  <xsl:param name="kc-adapter-template-path" />
  <xsl:param name="kc-server-template-path" />

  <xsl:template match="/*[local-name()='server']/*[local-name()='system-properties']">
    <xsl:copy>

      <xsl:comment> From Hawkular Nest: </xsl:comment>
      <xsl:variable name="hk-nest-props" select="./*" />
      <xsl:copy-of select="$hk-nest-props" />

      <xsl:variable name="kc-server-props" select="document($kc-server-template-path)/*[local-name()='server']/*[local-name()='system-properties']/*" />
      <xsl:if test="$kc-server-props">
        <xsl:comment> From Keycloak Server: </xsl:comment>
        <xsl:copy-of select="$kc-server-props" />
      </xsl:if>

      <xsl:variable name="kc-adapter-props" select="document($kc-adapter-template-path)/*[local-name()='server']/*[local-name()='system-properties']/*" />
      <xsl:if test="$kc-adapter-props">
        <xsl:comment> From Keycloak Adapter: </xsl:comment>
        <xsl:copy-of select="$kc-adapter-props" />
      </xsl:if>

      <xsl:comment> From Hawkular Accounts: </xsl:comment>
      <property name="keycloak.import">
        <xsl:attribute name="value">${keycloak.import:${jboss.home.dir}/standalone/configuration/hawkular-realm.json}</xsl:attribute>
      </property>
      <property name="keycloak.server.url">
        <xsl:attribute name="value">${keycloak.server.url:http://localhost:8080/auth}</xsl:attribute>
      </property>
      <property name="hawkular.events.listener.rest.endpoint">
        <xsl:attribute name="value">http://localhost:8080/hawkular-accounts-events-backend/events</xsl:attribute>
      </property>
      <property name="secretstore.redirectTo">
        <xsl:attribute name="value">${secretstore.redirectTo:/hawkular-ui/tokens/{tokenId}}</xsl:attribute>
      </property>
      <property name="secretstore.parametersToPersist">
        <xsl:attribute name="value">${secretstore.parametersToPersist:Hawkular-Persona}</xsl:attribute>
      </property>
    </xsl:copy>
  </xsl:template>

  <!-- copy everything else as-is -->
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
