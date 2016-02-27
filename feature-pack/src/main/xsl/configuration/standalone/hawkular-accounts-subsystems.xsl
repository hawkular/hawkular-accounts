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

  <xsl:param name="kc-adapter-subsystems-path" />
  <xsl:param name="kc-server-subsystems-path" />

  <xsl:template match="/*[local-name()='config']/*[local-name()='subsystems']">
    <xsl:copy>

      <!-- First put the merged set of subsystems into hk-accounts-subsystems variable -->
      <xsl:variable name="hk-accounts-subsystems">

        <!-- The base set of subsystems from KC Server -->
        <xsl:variable name="kc-server-subsystems" select="document($kc-server-subsystems-path)/*[local-name()='config']/*[local-name()='subsystems']/*" />
        <xsl:comment> The base set of subsystems from KC Server </xsl:comment>
        <!-- The experession in @select implements the minus operation for node sets - i.e. $kc-server-subsystems minus $hk-nest-subsystems -->
        <xsl:copy-of select="$kc-server-subsystems" />


        <!-- Add those subsystems from KC Adapter that are not there already -->
        <xsl:variable name="kc-adapter-subsystems" select="document($kc-adapter-subsystems-path)/*[local-name()='config']/*[local-name()='subsystems']/*" />
        <xsl:comment> Required by Keycloak Adapter: </xsl:comment>
        <xsl:copy-of select="$kc-adapter-subsystems[not(text() = $kc-server-subsystems/text())]" />


        <!-- $kc-server-plus-adapter will contain the union of KC Server subsystems with all those subsystems from KC Adapter that are not in Server already -->
        <xsl:variable name="kc-server-plus-adapter" select="$kc-server-subsystems | $kc-adapter-subsystems[not(text() = $kc-server-subsystems/text())]" />


        <!-- Add those subsystems from Nest that are not there already -->
        <xsl:variable name="hk-nest-subsystems" select="./*[not(text() = $kc-server-plus-adapter/text())]" />

        <xsl:comment> Required by Hawkular Nest: </xsl:comment>
        <xsl:copy-of select="$hk-nest-subsystems" />

        <xsl:comment> Required by Hawkular Accounts: </xsl:comment>
        <subsystem>hawkular-accounts-messaging-activemq.xml</subsystem>
      </xsl:variable>

      <!-- Second, we want to remove some subsystems or replace them with ours -->
      <xsl:for-each select="$hk-accounts-subsystems"><!-- no idea why select="$hk-accounts-subsystems/*" does not work -->
        <xsl:for-each select="./* | comment()">
          <xsl:message>seeing element <xsl:value-of select="local-name()" /> with text <xsl:value-of select="text()" /> and children count <xsl:value-of select="count(child::*)" /> </xsl:message>
          <xsl:choose>
            <xsl:when test="text()='datasources.xml'">
              <!-- remove and use keycloak-datasources.xml -->
            </xsl:when>
            <xsl:when test="text()='infinispan.xml'">
              <!-- remove and use keycloak-infinispan.xml -->
            </xsl:when>
            <xsl:when test="text()='ejb3.xml'">
              <subsystem>hawkular-accounts-ejb3.xml</subsystem>
            </xsl:when>
            <xsl:when test="text()='security.xml'">
              <subsystem>hawkular-accounts-security.xml</subsystem>
            </xsl:when>
            <xsl:when test="text()='keycloak-adapter.xml'">
              <subsystem>hawkular-accounts-keycloak-adapter.xml</subsystem>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="." />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>


  <!-- copy everything else as-is -->
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
