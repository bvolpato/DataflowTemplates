/*
 * Copyright (C) 2022 Google LLC
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
package com.google.api.services.datastream.v1.model;

/**
 * Cloud Storage bucket profile.
 *
 * <p>This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the Datastream API. For a detailed explanation see: <a
 * href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class GcsProfile extends com.google.api.client.json.GenericJson {

  /** Required. The Cloud Storage bucket name. The value may be {@code null}. */
  @com.google.api.client.util.Key private java.lang.String bucket;

  /** The root path inside the Cloud Storage bucket. The value may be {@code null}. */
  @com.google.api.client.util.Key private java.lang.String rootPath;

  /**
   * Required. The Cloud Storage bucket name.
   *
   * @return value or {@code null} for none
   */
  public java.lang.String getBucket() {
    return bucket;
  }

  /**
   * Required. The Cloud Storage bucket name.
   *
   * @param bucket bucket or {@code null} for none
   */
  public GcsProfile setBucket(java.lang.String bucket) {
    this.bucket = bucket;
    return this;
  }

  /**
   * The root path inside the Cloud Storage bucket.
   *
   * @return value or {@code null} for none
   */
  public java.lang.String getRootPath() {
    return rootPath;
  }

  /**
   * The root path inside the Cloud Storage bucket.
   *
   * @param rootPath rootPath or {@code null} for none
   */
  public GcsProfile setRootPath(java.lang.String rootPath) {
    this.rootPath = rootPath;
    return this;
  }

  @Override
  public GcsProfile set(String fieldName, Object value) {
    return (GcsProfile) super.set(fieldName, value);
  }

  @Override
  public GcsProfile clone() {
    return (GcsProfile) super.clone();
  }
}
