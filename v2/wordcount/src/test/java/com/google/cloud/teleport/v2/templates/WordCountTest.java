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

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.values.PCollection;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test cases for the {@link WordCount} class. */
@RunWith(JUnit4.class)
public final class WordCountTest {

  @Rule public final transient TestPipeline pipeline = TestPipeline.create();

  @Rule public final ExpectedException expectedException = ExpectedException.none();

  @ClassRule public static TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testWordCount_returnsValidCount() throws IOException {
    // Arrange
    String filePath = tempFolder.newFile().getAbsolutePath();
    writeToFile(filePath, Arrays.asList("Beam Pipeline", "Beam Java Sdk"));
    PCollection<String> inputLines = pipeline.apply("Read Lines", TextIO.read().from(filePath));

    // Act
    PCollection<String> results = WordCount.applyTransforms(inputLines);

    // Assert
    PAssert.that(results)
        .satisfies(
            pcollection -> {
              List<String> result = new ArrayList<>();
              pcollection.iterator().forEachRemaining(result::add);

              String[] expected = {"Beam: 2", "Java: 1", "Pipeline: 1", "Sdk: 1"};

              assertThat(result.size()).isEqualTo(4);
              assertThat(result).containsExactlyElementsIn(expected);

              return null;
            });

    pipeline.run();
  }

  private void writeToFile(String filePath, List<String> lines) throws IOException {
    String newlineCharacter = "\n";
    try (FileWriter fileWriter = new FileWriter(new File(filePath))) {
      for (String line : lines) {
        fileWriter.write(line + newlineCharacter);
      }
    }
  }
}
