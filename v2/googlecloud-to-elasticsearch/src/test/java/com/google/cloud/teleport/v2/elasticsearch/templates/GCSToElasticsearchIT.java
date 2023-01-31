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
package com.google.cloud.teleport.v2.elasticsearch.templates;

import static com.google.cloud.teleport.it.PipelineUtils.createJobName;
import static com.google.cloud.teleport.it.matchers.TemplateAsserts.assertThatPipeline;
import static com.google.cloud.teleport.it.matchers.TemplateAsserts.assertThatRecords;
import static com.google.cloud.teleport.it.matchers.TemplateAsserts.assertThatResult;
import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.teleport.it.TemplateTestBase;
import com.google.cloud.teleport.it.bigquery.BigQueryResourceManager;
import com.google.cloud.teleport.it.bigquery.DefaultBigQueryResourceManager;
import com.google.cloud.teleport.it.elasticsearch.DefaultElasticsearchResourceManager;
import com.google.cloud.teleport.it.elasticsearch.ElasticsearchResourceManager;
import com.google.cloud.teleport.it.launcher.PipelineLauncher.LaunchConfig;
import com.google.cloud.teleport.it.launcher.PipelineLauncher.LaunchInfo;
import com.google.cloud.teleport.it.launcher.PipelineOperator.Result;
import com.google.cloud.teleport.metadata.TemplateIntegrationTest;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration test for {@link GCSToElasticsearch}. */
@Category(TemplateIntegrationTest.class)
@TemplateIntegrationTest(GCSToElasticsearch.class)
@RunWith(JUnit4.class)
public final class GCSToElasticsearchIT extends TemplateTestBase {

  private static BigQueryResourceManager bigQueryClient;
  private static ElasticsearchResourceManager elasticsearchResourceManager;

  @Before
  public void setup() {
    bigQueryClient =
        DefaultBigQueryResourceManager.builder(testName.getMethodName(), PROJECT)
            .setCredentials(credentials)
            .build();
    elasticsearchResourceManager =
        DefaultElasticsearchResourceManager.builder(testId).setHost(HOST_IP).build();
  }

  @After
  public void tearDown() {
    bigQueryClient.cleanupAll();
    elasticsearchResourceManager.cleanupAll();
  }

  @Test
  public void testElasticsearchCsvWithoutHeaders() throws IOException {
    // Arrange
    artifactClient.uploadArtifact(
        "input/no_header_10.csv",
        Resources.getResource("GCSToElasticsearch/no_header_10.csv").getPath());
    artifactClient.uploadArtifact(
        "input/elasticUdf.js", Resources.getResource("GCSToElasticsearch/elasticUdf.js").getPath());
    String indexName = createJobName(testName.getMethodName());
    elasticsearchResourceManager.createIndex(indexName);
    bigQueryClient.createDataset(REGION);

    LaunchConfig.Builder options =
        LaunchConfig.builder(testName, specPath)
            .addParameter("inputFileSpec", getGcsPath("input") + "/*.csv")
            .addParameter("inputFormat", "csv")
            .addParameter("containsHeaders", "false")
            .addParameter("deadletterTable", PROJECT + ":" + bigQueryClient.getDatasetId() + ".dlq")
            .addParameter("delimiter", ",")
            .addParameter("connectionUrl", elasticsearchResourceManager.getUri())
            .addParameter("index", indexName)
            .addParameter("javascriptTextTransformGcsPath", getGcsPath("input/elasticUdf.js"))
            .addParameter("javascriptTextTransformFunctionName", "transform")
            .addParameter("apiKey", "elastic");

    // Act
    LaunchInfo info = launchTemplate(options);
    assertThatPipeline(info).isRunning();

    Result result = pipelineOperator().waitUntilDone(createConfig(info));

    // Assert
    assertThatResult(result).isLaunchFinished();

    assertThat(elasticsearchResourceManager.count(indexName)).isEqualTo(10);
    assertThatRecords(elasticsearchResourceManager.fetchAll(indexName))
        .hasRecordsUnordered(List.of(Map.of("id", "001", "state", "CA", "price", 3.65)));
  }

  @Test
  public void testElasticsearchCsvWithHeaders() throws IOException {
    // Arrange
    artifactClient.uploadArtifact(
        "input/with_headers_10.csv",
        Resources.getResource("GCSToElasticsearch/with_headers_10.csv").getPath());
    String indexName = createJobName(testName.getMethodName());
    elasticsearchResourceManager.createIndex(indexName);
    bigQueryClient.createDataset(REGION);

    LaunchConfig.Builder options =
        LaunchConfig.builder(testName, specPath)
            .addParameter("inputFileSpec", getGcsPath("input") + "/*.csv")
            .addParameter("inputFormat", "csv")
            .addParameter("containsHeaders", "true")
            .addParameter("deadletterTable", PROJECT + ":" + bigQueryClient.getDatasetId() + ".dlq")
            .addParameter("delimiter", ",")
            .addParameter("connectionUrl", elasticsearchResourceManager.getUri())
            .addParameter("index", indexName)
            .addParameter("apiKey", "elastic");

    // Act
    LaunchInfo info = launchTemplate(options);
    assertThatPipeline(info).isRunning();

    Result result = pipelineOperator().waitUntilDone(createConfig(info));

    // Assert
    assertThatResult(result).isLaunchFinished();

    assertThat(elasticsearchResourceManager.count(indexName)).isEqualTo(10);
    assertThatRecords(elasticsearchResourceManager.fetchAll(indexName))
        .hasRecordsUnordered(List.of(Map.of("id", "001", "state", "CA", "price", 3.65)));
  }
}
