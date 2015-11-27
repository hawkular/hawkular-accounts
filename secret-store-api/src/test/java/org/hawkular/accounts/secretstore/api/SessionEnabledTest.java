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
package org.hawkular.accounts.secretstore.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.cassandra.service.EmbeddedCassandraService;
import org.apache.thrift.transport.TTransportException;
import org.junit.Before;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

/**
 * @author Juraci Paixão Kröhling
 */
@SuppressWarnings("Duplicates")
public class SessionEnabledTest {
    static boolean prepared;
    static Session session;

    @Before
    public void prepareCassandra() throws IOException, TTransportException, InterruptedException {
        if (prepared) {
            return;
        }

        startServerIfNotRunning();
        cleanDatabase();
        prepared = true;
    }

    private void startServerIfNotRunning() throws IOException, TTransportException, InterruptedException {
        try {
            session = new Cluster.Builder()
                    .addContactPoints("localhost")
                    .withProtocolVersion(ProtocolVersion.V3)
                    .build().connect();
        } catch (NoHostAvailableException e) {
            String cassandraYmlLocation = findPathForCassandraYaml("./cassandra.yml");
            if (null == cassandraYmlLocation || cassandraYmlLocation.isEmpty()) {
                cassandraYmlLocation = findPathForCassandraYaml("./api/target/test-classes/cassandra.yml");
            }

            if (null == cassandraYmlLocation || cassandraYmlLocation.isEmpty()) {
                throw new IllegalArgumentException("Could not find a cassandra.yml");
            }

            System.setProperty("cassandra.config", "file://" + cassandraYmlLocation);
            EmbeddedCassandraService service = new EmbeddedCassandraService();
            service.start();

            session = new Cluster.Builder()
                    .addContactPoints("localhost")
                    .withPort(9142)
                    .withProtocolVersion(ProtocolVersion.V3)
                    .build().connect();
        }
    }

    public void cleanDatabase() throws IOException {
        session
                .getCluster().getMetadata().getKeyspaces()
                .stream()
                .filter(k -> k.getName().equals("secretstore"))
                .limit(1) // once we find it, no need to keep going through the stream
                .forEach(k -> session.execute("DROP KEYSPACE secretstore"));

        InputStream input = getClass().getResourceAsStream("/secret-store.cql");
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            String content = buffer.lines().collect(Collectors.joining("\n"));
            for (String cql : content.split("(?m)^-- #.*$")) {
                if (!cql.startsWith("--")) {
                    session.execute(cql);
                }
            }
        }
    }

    private String findPathForCassandraYaml(String pathToStart) throws IOException {
        File[] rootDirectories = File.listRoots();

        File file = new File(pathToStart);
        if (file.exists()) {
            return file.getAbsolutePath();
        } else {
            for (File root : rootDirectories) {
                String canonicalPathParent = file.getCanonicalFile().getParent();
                if (root.getPath().equals(canonicalPathParent)) {
                    return null;
                }
            }
            return findPathForCassandraYaml("../" + file.getParent() + "/" + file.getName());
        }
    }
}
