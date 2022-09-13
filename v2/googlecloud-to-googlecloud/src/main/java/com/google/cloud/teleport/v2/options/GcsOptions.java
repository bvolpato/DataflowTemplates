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
    Optional<String> getGcsDeadletterDirectory();

    void setGcsDeadletterDirectory(String gcsDeadletterDirectory);
  }
}
