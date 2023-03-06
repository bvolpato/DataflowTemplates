BigQuery to Bigtable Template
---
A pipeline to export a BigQuery table into Bigtable.

:memo: This is a Google-provided template! Please
check [Provided templates documentation](https://cloud.google.com/dataflow/docs/guides/templates/provided-templates)
on how to use it without having to build from sources.

:bulb: This is a generated documentation based
on [Metadata Annotations](https://github.com/GoogleCloudPlatform/DataflowTemplates#metadata-annotations)
. Do not change this file directly.

## Parameters

### Mandatory Parameters

* **readQuery** (Input SQL query): SQL query in standard SQL to pull data from BigQuery.
* **readIdColumn** (Unique identifier column): Name of the BigQuery column storing the unique identifier of the row.
* **bigtableWriteProjectId** (Project ID): The ID of the Google Cloud project of the Cloud Bigtable instance that you want to write data to.
* **bigtableWriteInstanceId** (Instance ID): The ID of the Cloud Bigtable instance that contains the table.
* **bigtableWriteAppProfile** (Bigtable App Profile): Bigtable App Profile to use for the export. Defaults to: default.
* **bigtableWriteTableId** (Table ID): The ID of the Cloud Bigtable table to write.

### Optional Parameters

* **bigtableWriteColumnFamily** (The Bigtable Column Family): This specifies the column family to write data into.
* **bigtableBulkWriteLatencyTargetMs** (Bigtable's latency target in milliseconds for latency-based throttling): This enables latency-based throttling and specifies the target latency.
* **bigtableBulkWriteMaxRowKeyCount** (The max number of row keys in a Bigtable batch write operation): This sets the max number of row keys in a Bigtable batch write operation.
* **bigtableBulkWriteMaxRequestSizeBytes** (The max amount of bytes in a Bigtable batch write operation): This sets the max amount of bytes in a Bigtable batch write operation.
* **bigtableRpcAttemptTimeoutMs** (The timeout for an RPC attempt in milliseconds): This sets the timeout for an RPC attempt in milliseconds.
* **bigtableRpcTimeoutMs** (The total timeout for an RPC operation in milliseconds): This sets the total timeout for an RPC operation in milliseconds.
* **bigtableAdditionalRetryCodes** (The additional retry codes): This sets the additional retry codes, separated by ',' (Example: RESOURCE_EXHAUSTED,DEADLINE_EXCEEDED).

## Getting Started

### Requirements

* Java 11
* Maven
* Valid resources for mandatory parameters.
* [gcloud CLI](https://cloud.google.com/sdk/gcloud), and execution of the
  following command:
    * `gcloud auth login`

This README uses
the [Templates Plugin](https://github.com/GoogleCloudPlatform/DataflowTemplates#templates-plugin)
. Install the plugin with the following command to proceed:

```shell
mvn clean install -pl plugins/templates-maven-plugin -am
```

### Building Template

This template is a Flex Template, meaning that the pipeline code will be
containerized and the container will be executed on Dataflow. Please
check [Use Flex Templates](https://cloud.google.com/dataflow/docs/guides/templates/using-flex-templates)
for more information.

#### Staging the Template

If the plan is to just stage the template (i.e., make it available to use) by
the `gcloud` command or Dataflow "Create job from template" UI,
the `-PtemplatesStage` profile should be used:

```shell
export PROJECT=<my-project>
export BUCKET_NAME=<bucket-name>

mvn clean package -PtemplatesStage  \
-DskipTests \
-DprojectId="$PROJECT" \
-DbucketName="$BUCKET_NAME" \
-DstagePrefix="templates" \
-DtemplateName="BigQuery_to_Bigtable" \
-pl v2/bigquery-to-bigtable -am
```

The command should print what is the template location on Cloud Storage:

```
Flex Template was staged! gs://{BUCKET}/{PATH}
```


#### Running the Template

**Using the staged template**:

You can use the path above to share or run the template.

To start a job with the template at any time using `gcloud`, you can use:

```shell
export TEMPLATE_SPEC_GCSPATH="gs://$BUCKET_NAME/templates/flex/BigQuery_to_Bigtable"
export PROJECT=<my-project>
export BUCKET_NAME=<bucket-name>
export REGION=us-central1

### Mandatory
export READ_QUERY=<readQuery>
export READ_ID_COLUMN=<readIdColumn>
export BIGTABLE_WRITE_PROJECT_ID=<bigtableWriteProjectId>
export BIGTABLE_WRITE_INSTANCE_ID=<bigtableWriteInstanceId>
export BIGTABLE_WRITE_APP_PROFILE="default"
export BIGTABLE_WRITE_TABLE_ID=<bigtableWriteTableId>

### Optional
export BIGTABLE_WRITE_COLUMN_FAMILY=<bigtableWriteColumnFamily>
export BIGTABLE_BULK_WRITE_LATENCY_TARGET_MS=<bigtableBulkWriteLatencyTargetMs>
export BIGTABLE_BULK_WRITE_MAX_ROW_KEY_COUNT=<bigtableBulkWriteMaxRowKeyCount>
export BIGTABLE_BULK_WRITE_MAX_REQUEST_SIZE_BYTES=<bigtableBulkWriteMaxRequestSizeBytes>
export BIGTABLE_RPC_ATTEMPT_TIMEOUT_MS=<bigtableRpcAttemptTimeoutMs>
export BIGTABLE_RPC_TIMEOUT_MS=<bigtableRpcTimeoutMs>
export BIGTABLE_ADDITIONAL_RETRY_CODES=<bigtableAdditionalRetryCodes>

gcloud dataflow flex-template run "bigquery-to-bigtable-job" \
  --project "$PROJECT" \
  --region "$REGION" \
  --template-file-gcs-location "$TEMPLATE_SPEC_GCSPATH" \
  --parameters "readQuery=$READ_QUERY" \
  --parameters "readIdColumn=$READ_ID_COLUMN" \
  --parameters "bigtableWriteProjectId=$BIGTABLE_WRITE_PROJECT_ID" \
  --parameters "bigtableWriteInstanceId=$BIGTABLE_WRITE_INSTANCE_ID" \
  --parameters "bigtableWriteAppProfile=$BIGTABLE_WRITE_APP_PROFILE" \
  --parameters "bigtableWriteTableId=$BIGTABLE_WRITE_TABLE_ID" \
  --parameters "bigtableWriteColumnFamily=$BIGTABLE_WRITE_COLUMN_FAMILY" \
  --parameters "bigtableBulkWriteLatencyTargetMs=$BIGTABLE_BULK_WRITE_LATENCY_TARGET_MS" \
  --parameters "bigtableBulkWriteMaxRowKeyCount=$BIGTABLE_BULK_WRITE_MAX_ROW_KEY_COUNT" \
  --parameters "bigtableBulkWriteMaxRequestSizeBytes=$BIGTABLE_BULK_WRITE_MAX_REQUEST_SIZE_BYTES" \
  --parameters "bigtableRpcAttemptTimeoutMs=$BIGTABLE_RPC_ATTEMPT_TIMEOUT_MS" \
  --parameters "bigtableRpcTimeoutMs=$BIGTABLE_RPC_TIMEOUT_MS" \
  --parameters "bigtableAdditionalRetryCodes=$BIGTABLE_ADDITIONAL_RETRY_CODES"
```


**Using the plugin**:

Instead of just generating the template in the folder, it is possible to stage
and run the template in a single command. This may be useful for testing when
changing the templates.

```shell
export PROJECT=<my-project>
export BUCKET_NAME=<bucket-name>
export REGION=us-central1

### Mandatory
export READ_QUERY=<readQuery>
export READ_ID_COLUMN=<readIdColumn>
export BIGTABLE_WRITE_PROJECT_ID=<bigtableWriteProjectId>
export BIGTABLE_WRITE_INSTANCE_ID=<bigtableWriteInstanceId>
export BIGTABLE_WRITE_APP_PROFILE="default"
export BIGTABLE_WRITE_TABLE_ID=<bigtableWriteTableId>

### Optional
export BIGTABLE_WRITE_COLUMN_FAMILY=<bigtableWriteColumnFamily>
export BIGTABLE_BULK_WRITE_LATENCY_TARGET_MS=<bigtableBulkWriteLatencyTargetMs>
export BIGTABLE_BULK_WRITE_MAX_ROW_KEY_COUNT=<bigtableBulkWriteMaxRowKeyCount>
export BIGTABLE_BULK_WRITE_MAX_REQUEST_SIZE_BYTES=<bigtableBulkWriteMaxRequestSizeBytes>
export BIGTABLE_RPC_ATTEMPT_TIMEOUT_MS=<bigtableRpcAttemptTimeoutMs>
export BIGTABLE_RPC_TIMEOUT_MS=<bigtableRpcTimeoutMs>
export BIGTABLE_ADDITIONAL_RETRY_CODES=<bigtableAdditionalRetryCodes>

mvn clean package -PtemplatesRun \
-DskipTests \
-DprojectId="$PROJECT" \
-DbucketName="$BUCKET_NAME" \
-Dregion="$REGION" \
-DjobName="bigquery-to-bigtable-job" \
-DtemplateName="BigQuery_to_Bigtable" \
-Dparameters="readQuery=$READ_QUERY,readIdColumn=$READ_ID_COLUMN,bigtableWriteProjectId=$BIGTABLE_WRITE_PROJECT_ID,bigtableWriteInstanceId=$BIGTABLE_WRITE_INSTANCE_ID,bigtableWriteAppProfile=$BIGTABLE_WRITE_APP_PROFILE,bigtableWriteTableId=$BIGTABLE_WRITE_TABLE_ID,bigtableWriteColumnFamily=$BIGTABLE_WRITE_COLUMN_FAMILY,bigtableBulkWriteLatencyTargetMs=$BIGTABLE_BULK_WRITE_LATENCY_TARGET_MS,bigtableBulkWriteMaxRowKeyCount=$BIGTABLE_BULK_WRITE_MAX_ROW_KEY_COUNT,bigtableBulkWriteMaxRequestSizeBytes=$BIGTABLE_BULK_WRITE_MAX_REQUEST_SIZE_BYTES,bigtableRpcAttemptTimeoutMs=$BIGTABLE_RPC_ATTEMPT_TIMEOUT_MS,bigtableRpcTimeoutMs=$BIGTABLE_RPC_TIMEOUT_MS,bigtableAdditionalRetryCodes=$BIGTABLE_ADDITIONAL_RETRY_CODES" \
-pl v2/bigquery-to-bigtable -am
```
