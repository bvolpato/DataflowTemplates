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
package com.google.cloud.teleport.v2.templates;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.cloud.teleport.metadata.Template;
import com.google.cloud.teleport.metadata.TemplateCategory;
import com.google.cloud.teleport.metadata.TemplateParameter;
import com.google.cloud.teleport.v2.templates.WordCount.WordCountOptions;
import com.google.cloud.teleport.v2.transforms.WordCountTransforms;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Word count template. */
@Template(
    name = "Word_Count_Flex",
    category = TemplateCategory.GET_STARTED,
    displayName = "Word Count",
    description =
        "Batch pipeline to read text files from Cloud Storage and perform "
            + "frequency count on each of the words.",
    flexContainerName = "wordcount",
    optionsClass = WordCountOptions.class)
public final class WordCount {

  private static final Logger LOG = LoggerFactory.getLogger(WordCount.class);

  /**
   * The {@link WordCountOptions} class provides the custom execution options passed by the executor
   * at the command-line.
   */
  public interface WordCountOptions extends PipelineOptions {

    @TemplateParameter.GcsReadFile(
        order = 1,
        description = "Input file(s) in Cloud Storage",
        helpText =
            "The input file pattern Dataflow reads from. Use the example file "
                + "(gs://dataflow-samples/shakespeare/kinglear.txt) or enter the path to your own "
                + "using the same format: gs://your-bucket/your-file.txt")
    String getInputFile();

    void setInputFile(String value);

    @TemplateParameter.GcsWriteFolder(
        order = 2,
        description = "Output Cloud Storage file prefix",
        helpText = "Path and filename prefix for writing output files. Ex: gs://your-bucket/counts")
    String getOutputPath();

    void setOutputPath(String value);

    @TemplateParameter.Integer(
        order = 3,
        optional = true,
        description = "Maximum output shards",
        helpText =
            "The maximum number of output shards produced when writing. A higher number of shards"
                + " means higher throughput for writing to Cloud Storage, but potentially higher"
                + " data aggregation cost across shards when processing output Cloud Storage"
                + " files. Default is runner dependent.")
    @Default.Integer(-1)
    int getNumShards();

    void setNumShards(int value);
  }

  /**
   * The main entry-point for pipeline execution.
   *
   * @param args command-line args passed by the executor.
   */
  public static void main(String[] args) {
    LOG.info("Starting WordCount...");

    WordCountOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(WordCountOptions.class);

    LOG.info("Options parsed: {}, building pipeline...", options);

    run(options);
  }

  /**
   * Runs the pipeline to completion with the specified options. This method does not wait until the
   * pipeline is finished before returning. Invoke {@code result.waitUntilFinish()} on the result
   * object to block until the pipeline is finished running if blocking programmatic execution is
   * required.
   *
   * @param options the execution options.
   * @return the pipeline result.
   */
  public static PipelineResult run(WordCountOptions options) {
    checkNotNull(options, "options argument to run method cannot be null.");
    Pipeline pipeline = Pipeline.create(options);

    PCollection<String> inputLines =
        pipeline.apply("ReadLines", TextIO.read().from(options.getInputFile()));

    PCollection<String> wordsCount = applyTransforms(inputLines);

    TextIO.Write writer = TextIO.write().to(options.getOutputPath());
    if (options.getNumShards() > 0) {
      writer = writer.withNumShards(options.getNumShards());
    }

    wordsCount.apply("WriteCounts", writer);

    LOG.info("Pipeline created, running pipeline...");

    return pipeline.run();
  }

  /**
   * Applies set of transforms on the given input to derive the expected output.
   *
   * @param lines Collection of text lines
   * @return the count of words with each line representing word and count in the form word: count.
   */
  public static PCollection<String> applyTransforms(PCollection<String> lines) {
    return lines
        .apply(new WordCountTransforms.CountWords())
        .apply(MapElements.via(new FormatAsTextFn()));
  }

  /** A SimpleFunction that converts a Word and Count into a printable string. */
  private static class FormatAsTextFn extends SimpleFunction<KV<String, Long>, String> {

    @Override
    public String apply(KV<String, Long> input) {
      return input.getKey() + ": " + input.getValue();
    }
  }
}
