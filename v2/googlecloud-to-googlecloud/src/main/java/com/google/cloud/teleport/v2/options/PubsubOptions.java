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

/** Options for Teleport PubsubIO. */
public class PubsubOptions {

  /** Options for using Pub/Sub as a deadletter sink. */
  public interface PubsubWriteDeadletterTopicOptions extends PipelineOptions {

    @Description(
        "Parameter which specifies if deadletter should be written to Pub/Sub. Default: true.")
    @Default.Boolean(true)
    Boolean getEnablePubsubDeadletter();

    void setEnablePubsubDeadletter(Boolean enablePubsubDeadletter);

    @Description(
        "The Cloud Pub/Sub topic to publish deadletter records to. "
            + "The name should be in the format of "
            + "projects/<project-id>/topics/<topic-name>.")
    @Default.String("")
    String getOutputDeadletterTopic();

    void setOutputDeadletterTopic(String deadletterTopic);
  }
}
