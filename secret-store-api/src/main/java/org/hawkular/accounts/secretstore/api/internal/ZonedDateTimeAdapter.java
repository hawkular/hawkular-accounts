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
package org.hawkular.accounts.secretstore.api.internal;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;

/**
 * JPA adapter for converting {@link ZonedDateTime} into {@link Timestamp}.
 *
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class ZonedDateTimeAdapter {
    public Timestamp convertToDatabaseColumn(ZonedDateTime attribute) {
        if (null == attribute) {
            return null;
        }
        return Timestamp.valueOf(attribute.toLocalDateTime());
    }

    public ZonedDateTime convertToEntityAttribute(Timestamp dbData) {
        if (null == dbData) {
            return null;
        }
        return ZonedDateTime.of(dbData.toLocalDateTime(), ZoneOffset.UTC);
    }

    public ZonedDateTime convertToEntityAttribute(Date dbData) {
        if (null == dbData) {
            return null;
        }
        return ZonedDateTime.ofInstant(dbData.toInstant(), ZoneOffset.UTC);
    }
}
