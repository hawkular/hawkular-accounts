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

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

/**
 * @author Juraci Paixão Kröhling
 */
public class ZonedDateTimeXmlAdapterTest {

    @Test
    public void testMarshal() {
        ZonedDateTimeXmlAdapter adapter = new ZonedDateTimeXmlAdapter();

        // 2015-01-01T00:00:00Z
        String marshalled = adapter.marshal(ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        assertEquals("2015-01-01T00:00:00Z", marshalled);

        marshalled = adapter.marshal(ZonedDateTime.of(2015, 1, 1, 1, 0, 0, 0, ZoneId.of("Europe/Berlin")));
        assertEquals("2015-01-01T00:00:00Z", marshalled);
    }

    @Test
    public void testUnmarshal() {
        ZonedDateTimeXmlAdapter adapter = new ZonedDateTimeXmlAdapter();

        // 2015-01-01T00:00:00Z
        ZonedDateTime expected = ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime unmarshalled = adapter.unmarshal("2015-01-01T00:00:00Z");
        assertEquals(expected, unmarshalled);

        expected = ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Berlin"));
        unmarshalled = adapter.unmarshal("2015-01-01T00:00+01:00[Europe/Berlin]");
        assertEquals(expected, unmarshalled);

        expected = ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneId.of("+2"));
        unmarshalled = adapter.unmarshal("2015-01-01T00:00+02:00");
        assertEquals(expected, unmarshalled);
    }

}
