BigQuery to MongoDB Template
---
A batch pipeline which reads data rows from BigQuery and writes them to MongoDB as documents.

:memo: This is a Google-provided template! Please
check [Provided templates documentation](https://cloud.google.com/dataflow/docs/guides/templates/provided-templates)
on how to use it without having to build from sources.

:bulb: This is a generated documentation based
on [Metadata Annotations](https://github.com/GoogleCloudPlatform/DataflowTemplates#metadata-annotations)
. Do not change this file directly.

## Parameters

### Mandatory Parameters

* **mongoDbUri** (MongoDB Connection URI): URI to connect to MongoDB Atlas. Defaults to: mongouri.
* **database** (MongoDB Database): Database in MongoDB to store the collection. (Example: my-db). Defaults to: db.
* **collection** (MongoDB collection): Name of the collection inside MongoDB database. (Example: my-collection). Defaults to: collection.
* **inputTableSpec** (BigQuery source table): BigQuery source table spec. (Example: bigquery-project:dataset.input_table). Defaults to: bqtable.

### Optional Parameters


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
-DtemplateName="BigQuery_to_MongoDB" \
-pl v2/googlecloud-to-mongodb -am
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
export TEMPLATE_SPEC_GCSPATH="gs://$BUCKET_NAME/templates/flex/BigQuery_to_MongoDB"
export PROJECT=<my-project>
export BUCKET_NAME=<bucket-name>
export REGION=us-central1

### Mandatory
export MONGO_DB_URI="mongouri"
export DATABASE="db"
export COLLECTION="collection"
export INPUT_TABLE_SPEC="bqtable"

### Optional

gcloud dataflow flex-template run "bigquery-to-mongodb-job" \
  --project "$PROJECT" \
  --region "$REGION" \
  --template-file-gcs-location "$TEMPLATE_SPEC_GCSPATH" \
  --parameters "mongoDbUri=$MONGO_DB_URI" \
  --parameters "database=$DATABASE" \
  --parameters "collection=$COLLECTION" \
  --parameters "inputTableSpec=$INPUT_TABLE_SPEC"
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
export MONGO_DB_URI="mongouri"
export DATABASE="db"
export COLLECTION="collection"
export INPUT_TABLE_SPEC="bqtable"

### Optional

mvn clean package -PtemplatesRun \
-DskipTests \
-DprojectId="$PROJECT" \
-DbucketName="$BUCKET_NAME" \
-Dregion="$REGION" \
-DjobName="bigquery-to-mongodb-job" \
-DtemplateName="BigQuery_to_MongoDB" \
-Dparameters="mongoDbUri=$MONGO_DB_URI,database=$DATABASE,collection=$COLLECTION,inputTableSpec=$INPUT_TABLE_SPEC" \
-pl v2/googlecloud-to-mongodb -am
```
