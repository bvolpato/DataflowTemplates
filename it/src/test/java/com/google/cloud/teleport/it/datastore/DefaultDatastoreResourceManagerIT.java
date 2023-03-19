/*
 * Copyright (C) 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.it.datastore;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.QueryResults;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class DefaultDatastoreResourceManagerIT {

  @Test
  public void testInsert() throws IOException {
    DefaultDatastoreResourceManager resourceManager =
        DefaultDatastoreResourceManager.builder("person", DatastoreUtils.createTestId("testInsert"))
            .build();
    List<Entity> entities =
        resourceManager.insert(
            Map.of(
                1L,
                Entity.newBuilder().set("name", "John Doe").build(),
                2L,
                Entity.newBuilder().set("name", "Joan of Arc").build()));
    assertThat(entities).hasSize(2);

    resourceManager.cleanupAll();
  }

  @Test
  public void testInsertQuery() throws IOException {
    DefaultDatastoreResourceManager resourceManager =
        DefaultDatastoreResourceManager.builder(
                "person", DatastoreUtils.createTestId("testInsertQuery"))
            .build();
    List<Entity> entities =
        resourceManager.insert(Map.of(1L, Entity.newBuilder().set("name", "John Doe").build()));
    assertThat(entities).hasSize(1);

    QueryResults<Entity> queryResults = resourceManager.query("SELECT * from person");
    assertThat(queryResults).isNotNull();

    Entity person = queryResults.next();
    assertThat(person).isNotNull();

    assertThat(person.getKey().getId()).isEqualTo(1L);
    assertThat(person.getString("name")).isEqualTo("John Doe");

    resourceManager.cleanupAll();
  }

  @Test
  public void testInsertCleanUp() throws IOException {
    DefaultDatastoreResourceManager resourceManager =
        DefaultDatastoreResourceManager.builder(
                "person", DatastoreUtils.createTestId("testInsertCleanUp"))
            .build();
    List<Entity> entities =
        resourceManager.insert(Map.of(1L, Entity.newBuilder().set("name", "John Doe").build()));
    assertThat(entities).hasSize(1);

    resourceManager.cleanupAll();

    QueryResults<Entity> queryResults = resourceManager.query("SELECT * from person");
    assertThat(queryResults).isNotNull();
    assertThat(queryResults.hasNext()).isFalse();
  }
}
