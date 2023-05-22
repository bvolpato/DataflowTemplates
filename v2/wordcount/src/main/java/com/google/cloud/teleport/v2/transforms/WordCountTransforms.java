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
package com.google.cloud.teleport.v2.transforms;

import java.util.Arrays;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

/** A {@link WordCountTransforms} converts lines into tokens and counts words. */
public class WordCountTransforms {

  /**
   * A PTransform that converts a PCollection containing lines of text into a PCollection of word
   * counts.
   */
  public static class CountWords
      extends PTransform<PCollection<String>, PCollection<KV<String, Long>>> {

    @Override
    public PCollection<KV<String, Long>> expand(PCollection<String> lines) {

      // Convert lines of text into individual words.
      PCollection<String> words = lines.apply(ParDo.of(new ExtractWordsFn()));

      // Count the number of times each word occurs.
      return words.apply(Count.<String>perElement());
    }
  }

  static class ExtractWordsFn extends DoFn<String, String> {

    private final Counter emptyLines = Metrics.counter(ExtractWordsFn.class, "emptyLines");

    @ProcessElement
    public void processElement(@Element String line, OutputReceiver<String> receiver) {
      line = line.trim();
      if (line.isEmpty()) {
        emptyLines.inc();
      } else {
        // Split the line into words.
        String[] words = line.split("[^a-zA-Z']+");

        // Output each word encountered into the output PCollection.
        Arrays.stream(words).filter((word) -> !word.isEmpty()).forEach(receiver::output);
      }
    }
  }
}
