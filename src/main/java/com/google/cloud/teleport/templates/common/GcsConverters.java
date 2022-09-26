/*
 * Copyright (C) 2018 Google LLC
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
package com.google.cloud.teleport.templates.common;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.ValueProvider;

/** Options for Gcs utilities. */
public class GcsConverters {

  /** Options for using GCS as a deadletter sink. */
  public interface GcsWriteDeadletterTopicOptions extends PipelineOptions {

    @Description(
        "Parameter which specifies if deadletter should be written to Cloud Storage. Default: false.")
    @Default.Boolean(false)
    ValueProvider<Boolean> getEnableGcsDeadletter();

    void setEnableGcsDeadletter(ValueProvider<Boolean> enableGcsDeadletter);

    @Description(
        "The output location to write to deadletter records. For example, "
            + "gs://mybucket/somefolder/. Must be specified when Cloud Storage deadletter is enabled.")
    @Default.String("")
    ValueProvider<String> getGcsDeadletterDirectory();

    void setGcsDeadletterDirectory(ValueProvider<String> gcsDeadletterDirectory);

    @Description(
        "Parameter which specifies if deadletter writes to Cloud Storage must be notified through"
            + " a Cloud Pub/Sub topic. Default: false.")
    @Default.Boolean(false)
    ValueProvider<Boolean> getGcsDeadletterPubsubNotification();

    void setGcsDeadletterPubsubNotification(ValueProvider<Boolean> gcsDeadletterPubsubNotification);

    @Description(
        "The Cloud Pub/Sub topic to write deadletter notification messages. "
            + "The name should be in the format of projects/<project-id>/topics/<topic-name>. "
            + "Must be specified when Deadletter Pub/Sub Notification is enabled.")
    @Default.String("")
    ValueProvider<String> getGcsDeadletterPubsubTopic();

    void setGcsDeadletterPubsubTopic(ValueProvider<String> gcsDeadletterPubsubTopic);

  }
}
