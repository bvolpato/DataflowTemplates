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
package com.google.cloud.teleport.v2.options;

import java.util.Optional;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.ValueProvider;

/** Options for Teleport to Gcs integration. */
public class GcsOptions {

  /** Options for using GCS as a deadletter sink. */
  public interface GcsWriteDeadletterTopicOptions extends PipelineOptions {

    @Description(
        "Parameter which specifies if deadletter should be written to Cloud Storage. Default: false.")
    @Default.Boolean(false)
    Boolean getEnableGcsDeadletter();

    void setEnableGcsDeadletter(Boolean enableGcsDeadletter);

    @Description(
        "The output location to write to deadletter records. For example, "
            + "gs://mybucket/somefolder/. Must be specified when Cloud Storage deadletter is enabled.")
    @Default.String("")
    String getGcsDeadletterDirectory();

    void setGcsDeadletterDirectory(String gcsDeadletterDirectory);

    @Description(
        "The window duration in which deadletter data will be written. Defaults to 1m."
            + "Allowed formats are: "
            + "Ns (for seconds, example: 5s), "
            + "Nm (for minutes, example: 12m), "
            + "Nh (for hours, example: 2h).")
    @Default.String("1m")
    String getGcsDeadletterWindowDuration();

    void setGcsDeadletterWindowDuration(String gcsDeadletterWindowDuration);

    @Description(
        "Parameter which specifies if deadletter writes to Cloud Storage must be notified through"
            + " a Cloud Pub/Sub topic. Default: false.")
    @Default.Boolean(false)
    Boolean getGcsDeadletterPubsubNotification();

    void setGcsDeadletterPubsubNotification(Boolean gcsDeadletterPubsubNotification);

    @Description(
        "The Cloud Pub/Sub topic to write deadletter notification messages. "
            + "The name should be in the format of projects/<project-id>/topics/<topic-name>. "
            + "Must be specified when Deadletter Pub/Sub Notification is enabled.")
    @Default.String("")
    String getGcsDeadletterPubsubTopic();

    void setGcsDeadletterPubsubTopic(String gcsDeadletterPubsubTopic);

  }
}
