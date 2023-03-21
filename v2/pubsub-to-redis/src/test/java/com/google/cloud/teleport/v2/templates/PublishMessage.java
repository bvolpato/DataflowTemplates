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
package com.google.cloud.teleport.v2.templates;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PublishMessage {
  public static void main(String... args) throws Exception {
    /*
     * gcloud auth application-default login
     * Then use the generated credentials file as
     * -DGOOGLE_APPLICATION_CREDENTIALS=<credentials_file.json>
     */
    System.out.println(System.getProperty("GOOGLE_APPLICATION_CREDENTIALS"));
    publishWithConcurrencyControlExample(
        System.getProperty("projectId", "central-beach-194106"),
        System.getProperty("topicId", "pubsub-to-redis"));
  }

  public static void publishWithConcurrencyControlExample(String projectId, String topicId)
      throws IOException, ExecutionException, InterruptedException {
    TopicName topicName = TopicName.of(projectId, topicId);
    Publisher publisher = null;
    List<ApiFuture<String>> messageIdFutures = new ArrayList<>();

    try {
      // Provides an executor service for processing messages. The default
      // `executorProvider` used by the publisher has a default thread count of
      // 5 * the number of processors available to the Java virtual machine.
      ExecutorProvider executorProvider =
          InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(4).build();

      // `setExecutorProvider` configures an executor for the publisher.
      publisher =
          Publisher.newBuilder(topicName)
              .setExecutorProvider(executorProvider)
              .setEnableMessageOrdering(true)
              .build();

      // schedule publishing one message at a time : messages get automatically batched
      for (int i = 1; i <= 1; i++) {
        // String message = "{ \"name\": \"Allen Terleto\",\n" + "\"email\": \"allen@gmail.com\" }";

        String dataflowLogMessage =
            "{\n"
                + "  \"insertId\": \"4251196166436777832:674811:0:8121\",\n"
                + "  \"jsonPayload\": {\n"
                + "    \"message\": \"INFO: Starting PubSub-To-Redis Pipeline. Reading from subscription: projects/central-beach-194106/subscriptions/pubsub-to-redis\",\n"
                + "    \"line\": \"exec.go:66\"\n"
                + "  },\n"
                + "  \"resource\": {\n"
                + "    \"type\": \"dataflow_step\",\n"
                + "    \"labels\": {\n"
                + "      \"project_id\": \"central-beach-194106\",\n"
                + "      \"job_name\": \"pubsub-to-redis-20230509190435\",\n"
                + "      \"region\": \"us-central1\",\n"
                + "      \"job_id\": \"2023-05-09_16_04_38-12092580578624392451\",\n"
                + "      \"step_id\": "
                + "\""
                + i
                + "\"\n"
                + "    }\n"
                + "  },\n"
                + "  \"timestamp\": "
                + "\""
                + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())
                + "\",\n"
                + "  \"severity\": \"INFO\",\n"
                + "  \"labels\": {\n"
                + "    \"dataflow.googleapis.com/region\": \"us-central1\",\n"
                + "    \"dataflow.googleapis.com/job_id\": \"2023-05-09_16_04_38-12092580578624392451\",\n"
                + "    \"compute.googleapis.com/resource_type\": \"instance\",\n"
                + "    \"compute.googleapis.com/resource_name\": \"launcher-2023050916043812092580578624392451\",\n"
                + "    \"dataflow.googleapis.com/job_name\": \"pubsub-to-redis-20230509190435\",\n"
                + "    \"compute.googleapis.com/resource_id\": \"4251196166436777832\"\n"
                + "  },\n"
                + "  \"logName\": \"projects/central-beach-194106/logs/dataflow.googleapis.com%2Flauncher\",\n"
                + "  \"receiveTimestamp\": \"2023-05-09T23:06:13.573450968Z\"\n"
                + "}";
        ByteString data = ByteString.copyFromUtf8(dataflowLogMessage);
        PubsubMessage pubsubMessage =
            PubsubMessage.newBuilder()
                .setData(data)
                .putAttributes("key", "msg:" + i)
                .setOrderingKey("message " + i)
                .build();

        // Once published, returns a server-assigned message id (unique within the topic)
        ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
        messageIdFutures.add(messageIdFuture);
        Thread.sleep(1000);

        System.out.println("Publishing message " + i + ":\n" + pubsubMessage);
      }
    } finally {
      // Wait on any pending publish requests.
      List<String> messageIds = ApiFutures.allAsList(messageIdFutures).get();

      System.out.println("Published " + messageIds.size() + " messages with concurrency control.");

      if (publisher != null) {
        // When finished with the publisher, shutdown to free up resources.
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
  }
}
