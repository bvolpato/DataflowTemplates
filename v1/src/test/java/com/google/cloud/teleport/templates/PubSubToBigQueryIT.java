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
package com.google.cloud.teleport.templates;

import static com.google.cloud.teleport.it.matchers.TemplateAsserts.assertThatPipeline;
import static com.google.cloud.teleport.it.matchers.TemplateAsserts.assertThatRecords;
import static com.google.cloud.teleport.it.matchers.TemplateAsserts.assertThatResult;
import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.teleport.it.TemplateTestBase;
import com.google.cloud.teleport.it.bigquery.BigQueryResourceManager;
import com.google.cloud.teleport.it.bigquery.DefaultBigQueryResourceManager;
import com.google.cloud.teleport.it.conditions.BigQueryRowsCheck;
import com.google.cloud.teleport.it.launcher.PipelineLauncher.LaunchConfig;
import com.google.cloud.teleport.it.launcher.PipelineLauncher.LaunchInfo;
import com.google.cloud.teleport.it.launcher.PipelineOperator.Result;
import com.google.cloud.teleport.it.pubsub.DefaultPubsubResourceManager;
import com.google.cloud.teleport.it.pubsub.PubsubResourceManager;
import com.google.cloud.teleport.metadata.TemplateIntegrationTest;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration test for {@link PubSubToBigQuery} classic template. */
@Category(TemplateIntegrationTest.class)
@RunWith(JUnit4.class)
public final class PubSubToBigQueryIT extends TemplateTestBase {

  private static final int MESSAGES_COUNT = 10;
  private static final int BAD_MESSAGES_COUNT = 3;

  private static PubsubResourceManager pubsubResourceManager;
  private static BigQueryResourceManager bigQueryResourceManager;

  @Before
  public void setUp() throws IOException {
    pubsubResourceManager =
        DefaultPubsubResourceManager.builder(testName.getMethodName(), PROJECT)
            .credentialsProvider(credentialsProvider)
            .build();
    bigQueryResourceManager =
        DefaultBigQueryResourceManager.builder(testName.getMethodName(), PROJECT)
            .setCredentials(credentials)
            .build();

    artifactClient.uploadArtifact("udf-basic.js", Resources.getResource("udf-basic.js").getPath());
  }

  @After
  public void cleanUp() {
    pubsubResourceManager.cleanupAll();
    bigQueryResourceManager.cleanupAll();
  }

  @Test
  @TemplateIntegrationTest(value = PubSubToBigQuery.class, template = "PubSub_to_BigQuery")
  public void testTopicToBigQuery() throws IOException {
    // Arrange
    Map<String, Object> message = Map.of("job", testName.getMethodName(), "msg", "message");
    List<Field> bqSchemaFields =
        Arrays.asList(
            Field.of("job", StandardSQLTypeName.STRING),
            Field.of("msg", StandardSQLTypeName.STRING));
    Schema bqSchema = Schema.of(bqSchemaFields);

    TopicName topic = pubsubResourceManager.createTopic("input");
    bigQueryResourceManager.createDataset(REGION);
    TableId table = bigQueryResourceManager.createTable(testName.getMethodName(), bqSchema);

    LaunchConfig.Builder options =
        LaunchConfig.builder(testName, specPath)
            .addParameter("inputTopic", topic.toString())
            .addParameter("outputTableSpec", toTableSpec(table))
            .addParameter("javascriptTextTransformGcsPath", getGcsPath("udf-basic.js"))
            .addParameter("javascriptTextTransformFunctionName", "uppercaseName");

    // Act
    LaunchInfo info = launchTemplate(options);
    assertThatPipeline(info).isRunning();

    Result result =
        pipelineOperator()
            .waitForConditionAndFinish(
                createConfig(info),
                () -> {
                  ByteString messageData =
                      ByteString.copyFromUtf8(new JSONObject(message).toString());
                  pubsubResourceManager.publish(topic, ImmutableMap.of(), messageData);

                  return new BigQueryRowsCheck(bigQueryResourceManager, table, 1).get();
                });

    // Assert
    assertThatResult(result).meetsConditions();
    assertThatRecords(bigQueryResourceManager.readTable(table)).allMatch(message);
  }

  @Test
  @TemplateIntegrationTest(
      value = PubSubToBigQuery.class,
      template = "PubSub_Subscription_to_BigQuery")
  public void testSubscriptionToBigQuery() throws IOException {
    // Arrange
    List<Field> bqSchemaFields =
        Arrays.asList(
            Field.of("id", StandardSQLTypeName.INT64),
            Field.of("job", StandardSQLTypeName.STRING),
            Field.of("name", StandardSQLTypeName.STRING));
    Schema bqSchema = Schema.of(bqSchemaFields);

    TopicName topic = pubsubResourceManager.createTopic("input");
    SubscriptionName subscription = pubsubResourceManager.createSubscription(topic, "input-sub-1");
    bigQueryResourceManager.createDataset(REGION);
    TableId table = bigQueryResourceManager.createTable(testName.getMethodName(), bqSchema);
    TableId dlqTable =
        TableId.of(
            PROJECT,
            table.getDataset(),
            table.getTable() + PubSubToBigQuery.DEFAULT_DEADLETTER_TABLE_SUFFIX);

    LaunchConfig.Builder options =
        LaunchConfig.builder(testName, specPath)
            .addParameter("inputSubscription", subscription.toString())
            .addParameter("outputTableSpec", toTableSpec(table));

    // Act
    LaunchInfo info = launchTemplate(options);
    assertThatPipeline(info).isRunning();

    Result result =
        pipelineOperator()
            .waitForConditionsAndFinish(
                createConfig(info),
                new BigQueryRowsCheck(bigQueryResourceManager, table, MESSAGES_COUNT),
                new BigQueryRowsCheck(bigQueryResourceManager, dlqTable, BAD_MESSAGES_COUNT));

    // Assert
    assertThatResult(result).meetsConditions();
    TableResult records = bigQueryResourceManager.readTable(table);

    // Make sure record can be read and UDF changed name to uppercase
    assertThatRecords(records)
        .hasRecordsUnordered(
            List.of(Map.of("id", 1, "job", testName.getMethodName(), "name", "MESSAGE")));

    TableResult dlqRecords = bigQueryResourceManager.readTable(dlqTable);
    assertThat(dlqRecords.getValues().iterator().next().toString())
        .contains("Expected json literal but found");
  }
}
