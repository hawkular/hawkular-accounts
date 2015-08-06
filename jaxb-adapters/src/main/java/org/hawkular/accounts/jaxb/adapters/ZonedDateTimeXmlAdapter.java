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
package org.hawkular.accounts.jaxb.adapters;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAX-B adapter for ZonedDateTime, converting to and from the ISO Date Time format.
 *
 * @author Juraci Paixão Kröhling
 */
public class ZonedDateTimeXmlAdapter extends XmlAdapter<String, ZonedDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public ZonedDateTime unmarshal(String s) {
        if (null == s || s.isEmpty()) {
            return null;
        }

        return ZonedDateTime.parse(s, formatter);
    }

    /**
     * When marshalling, the provided zonedDateTime is normalized to UTC, so that the output is consistent between
     * dates and time zones.
     *
     * @param zonedDateTime    the zonedDateTime to marshall
     * @return                  the UTC-based date time in ISO format
     */
    @Override
    public String marshal(ZonedDateTime zonedDateTime) {
        if (null == zonedDateTime) {
            return null;
        }

        return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).format(formatter);
    }
}
