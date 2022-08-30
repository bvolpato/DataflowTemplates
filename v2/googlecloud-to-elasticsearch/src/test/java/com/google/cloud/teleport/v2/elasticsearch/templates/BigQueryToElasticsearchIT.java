/*
 * Copyright (C) 2021 Google LLC
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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.teleport.v2.elasticsearch.ElasticsearchTestUtils;
import com.google.cloud.teleport.v2.elasticsearch.options.BigQueryToElasticsearchOptions;
import java.io.IOException;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.PipelineResult.State;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/** Test cases for {@link BigQueryToElasticsearch}. */
public class BigQueryToElasticsearchIT {

  /** Original Elasticsearch port to get the mapping from the container. */
  private static final int ELASTICSEARCH_ORIGINAL_PORT = 9200;

  /**
   * Name of the table to read.
   *
   * <p>Seed: CREATE TABLE `cloud-teleport-testing`.`bigquery_testdata`.`shakespeare` AS (SELECT *
   * FROM `bigquery-public-data`.`samples`.`shakespeare`);
   */
  private static final String BIGQUERY_READ_TABLE =
      "cloud-teleport-testing:bigquery_testdata.shakespeare";

  /** Baseline: SELECT COUNT(*) from `cloud-teleport-testing`.`bigquery_testdata`.`shakespeare`;. */
  private static final int NUMBER_OF_RECORDS = 164656;

  @Rule
  public ElasticsearchContainer elasticsearch =
      new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss:7.10.2");

  @Rule public final TestName testName = new TestName();

  private RestHighLevelClient elasticsearchClient;

  @Before
  public void setup() {
    // Prepare the Elasticsearch Client
    RestClientBuilder restClient =
        RestClient.builder(
            new HttpHost(
                elasticsearch.getHost(), elasticsearch.getMappedPort(ELASTICSEARCH_ORIGINAL_PORT)));

    elasticsearchClient = new RestHighLevelClient(restClient);
  }

  /** Test the {@link BigQueryToElasticsearch} pipeline end-to-end. */
  @Test
  public void testBigQueryToElasticsearchE2E() throws IOException {
    // Arrange
    String indexName = ElasticsearchTestUtils.createIndexName(testName.getMethodName());
    String[] args =
        new String[] {
          "--inputTableSpec=" + BIGQUERY_READ_TABLE,
          "--connectionUrl=http://" + elasticsearch.getHttpHostAddress(),
          "--apiKey=none",
          "--index=" + indexName
        };

    // Act
    BigQueryToElasticsearchOptions options =
        PipelineOptionsFactory.fromArgs(args)
            .withValidation()
            .as(BigQueryToElasticsearchOptions.class);
    PipelineResult result = BigQueryToElasticsearch.run(options);

    // Assert
    assertThat(result.getState()).isEqualTo(State.DONE);

    // Refresh required to make sure queries run on the latest index information
    elasticsearchClient.indices().refresh(new RefreshRequest(), RequestOptions.DEFAULT);

    assertThat(
            elasticsearchClient
                .count(new CountRequest(indexName), RequestOptions.DEFAULT)
                .getCount())
        .isEqualTo(NUMBER_OF_RECORDS);
  }
}
