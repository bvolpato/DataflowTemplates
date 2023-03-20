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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auth.Credentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.GqlQuery;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query.ResultType;
import com.google.cloud.datastore.QueryResults;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultDatastoreResourceManager implements DatastoreResourceManager {

  private final String namespace;

  private final Datastore datastore;
  private final List<Key> keys;

  public DefaultDatastoreResourceManager(Builder builder) {
    this.namespace = builder.namespace;

    this.datastore =
        DatastoreOptions.newBuilder().setCredentials(builder.credentials).build().getService();
    this.keys = new ArrayList<>();
  }

  @VisibleForTesting
  DefaultDatastoreResourceManager(String namespace, Datastore datastore) {
    this.namespace = namespace;
    this.datastore = datastore;
    this.keys = new ArrayList<>();
  }

  @Override
  public List<Entity> insert(String kind, Map<Long, FullEntity<?>> entities) {
    List<Entity> created = new ArrayList<>();

    for (Map.Entry<Long, FullEntity<?>> entry : entities.entrySet()) {
      Key entityKey =
          datastore.newKeyFactory().setKind(kind).setNamespace(namespace).newKey(entry.getKey());
      Entity entity = Entity.newBuilder(entityKey, entry.getValue()).build();
      created.add(datastore.put(entity));
      keys.add(entityKey);
    }

    return created;
  }

  @Override
  public QueryResults<Entity> query(String gqlQuery) {
    return datastore.run(
        GqlQuery.newGqlQueryBuilder(ResultType.ENTITY, gqlQuery).setNamespace(namespace).build());
  }

  @Override
  public void cleanupAll() {
    datastore.delete(keys.toArray(new Key[0]));
  }

  public static Builder builder(String namespace) {
    checkArgument(!Strings.isNullOrEmpty(namespace), "namespace can not be empty");
    return new Builder(namespace);
  }

  public static final class Builder {

    private final String namespace;
    private Credentials credentials;

    private Builder(String namespace) {
      this.namespace = namespace;
    }

    public Builder credentials(Credentials credentials) {
      this.credentials = credentials;
      return this;
    }

    public DefaultDatastoreResourceManager build() {
      if (credentials == null) {
        throw new IllegalArgumentException(
            "Unable to find credentials. Please provide credentials to authenticate to GCP");
      }
      return new DefaultDatastoreResourceManager(this);
    }
  }
}
