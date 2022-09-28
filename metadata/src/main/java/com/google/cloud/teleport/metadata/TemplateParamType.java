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
package com.google.cloud.teleport.metadata;

/** Parameter types that can be used on Template options. */
public enum TemplateParamType {

  /** Generic text parameter. */
  TEXT,

  /** Cloud Storage glob to read file(s). */
  GCS_READ_FILE,

  /** Cloud Storage folder to read. */
  GCS_READ_FOLDER,

  /** Cloud Storage file to write. */
  GCS_WRITE_FILE,

  /** Cloud Storage folder to write. */
  GCS_WRITE_FOLDER,

  /** Pub/Sub Subscription to read. */
  PUBSUB_SUBSCRIPTION,

  /** Pub/Sub Topic to read or write. */
  PUBSUB_TOPIC,

  /** BigQuery table (not available on Dataflow UI yet). */
  BIGQUERY_TABLE,

  /** Boolean (not available on Dataflow UI yet). */
  BOOLEAN,

  /** Datetime (not available on Dataflow UI yet). */
  DATETIME,

  /** Duration (not available on Dataflow UI yet). */
  DURATION,

  /** Enum (not available on Dataflow UI yet). */
  ENUM,

  /** Integer (not available on Dataflow UI yet). */
  INTEGER,

  /** KMS Encryption Key (not available on Dataflow UI yet). */
  KMS_ENCRYPTION_KEY,

  /** Long (not available on Dataflow UI yet). */
  LONG,

  /** Password (not available on Dataflow UI yet). */
  PASSWORD,
}
