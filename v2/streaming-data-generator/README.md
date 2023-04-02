# Dataflow Streaming Data Generator
[StreamingDataGenerator](src/main/java/com/google/cloud/teleport/v2/templates/StreamingDataGenerator.java) -
templates generates either unlimited or fixed number of synthetic records/messages based on user specified
schema template at a specific QPS, encodes as either Json/Avro/Parquet and writes to one of the sinks
(PubSub/BigQuery/Google Cloud Storage) as per users choice.

Following are few use cases:

1. Simulate large scale real time event publishing to Pub/Sub to measure and determine the number and size of
   consumers required to process published events.

2. Generate synthetic data to systems like BigQuery, Google Cloud Storage to run either performance benchmarks
   / Proof of concepts.

Below table outlines supported sinks and output encoding formats:

|  | JSON | AVRO  | PARQUET |
| ---|:----:| :-----:|:-----:|
| Pub/Sub | Y | Y | |
| BigQuery | Y | |  |
| Google Cloud Storage | Y | Y | Y |

> Note the number of workers executing the pipeline must be large enough to support the supplied
> QPS. Use a general rule of 2,500 QPS per core in the worker pool when configuring your pipeline.

## Sample Pipeline DAG
![Pipeline DAG](img/pipeline-dag.png "Pipeline DAG")

## Getting Started

### Requirements

* Java 11
* Maven 3
* One of the following depending on Sink Type:
  - PubSub Topic
  - BigQuery Table
  - Google Cloud Storage Bucket

### Creating the Schema File
The schema file used to generate JSON messages with fake data is based on the
[json-data-generator](https://github.com/vincentrussell/json-data-generator) library. This library
allows for the structuring of a sample JSON schema and injection of common faker functions to
instruct the data generator of what type of fake data to create in each field. See the
json-data-generator [docs](https://github.com/vincentrussell/json-data-generator) for more
information on the faker functions.

#### Example Schema File
Below is an example schema file which generates fake game event payloads with random data.

```javascript
{
  "eventId": "{{uuid()}}",
  "eventTimestamp": {{timestamp()}},
  "ipv4": "{{ipv4()}}",
  "ipv6": "{{ipv6()}}",
  "country": "{{country()}}",
  "username": "{{username()}}",
  "quest": "{{random("A Break In the Ice", "Ghosts of Perdition", "Survive the Low Road")}}",
  "score": {{integer(100, 10000)}},
  "completed": {{bool()}}
}
```

#### Example Output Data
Based on the above schema, the below would be an example of a message which would be output to the
Pub/Sub topic.

```javascript
{
  "eventId": "5dacca34-163b-42cb-872e-fe3bad7bffa9",
  "eventTimestamp": 1537729128894,
  "ipv4": "164.215.241.55",
  "ipv6": "e401:58fc:93c5:689b:4401:206f:4734:2740",
  "country": "Montserrat",
  "username": "asellers",
  "quest": "A Break In the Ice",
  "score": 2721,
  "completed": false
}
```

#### Example Schema file to generate Pub/Sub message with attributes
In scenarios that require the pub/Sub message attributes, payload fields and attribute fields can be specified as shown
below. In this example attributes represents both payload fields (eventId, eventTime) and non payload fields (appId,playerId)
```javascript
{
    "payload": {
        "eventId": "{{put("eventId",uuid())}}",
        "eventTime": {{put("eventTime", timestamp())}},
        "username": "{{put("username ", username())}}",
        "ipv4": "{{ipv4()}}",
        "country": "{{country()}}",
        "score": {{ integer(0, 100) }},
        "completed": {{bool()}}
    },
    "attributes": {
        "eventId": "{{get("eventId")}}",
        "eventTime": {{get("eventTime")}},
        "appId": {{ integer(1, 10) }},
        "playerId": {{ long(900235, 99000990098) }},
    }
}
```
Based on the above schema, the message payload will be as shown below and attributes would be eventId, eventTime, appId, playerId
```javascript
{
  "eventId": "5dacca34-163b-42cb-872e-fe3bad7bffa9",
  "eventTimestamp": 1537729128894,
  "username": "asellers",
  "ipv4": "164.215.241.55",
  "country": "Montserrat",
  "score": 2721,
  "completed": false
}
```
* Note: Template checks for presence of "^\\{\"?payload\"?:.+\"?attributes\"?:.+" pattern to determine whether to populate attributes or not

#### Generate Avro Pub/Sub Messages
To generate Avro encoded Pub/Sub messages supply following additional parameters:
- --outputType=AVRO
- --avroSchemaLocation=gs://bucketname/prefix/filename.avsc

Below is the example of avro schema corresponding to above message schema:
```javascript
{
     "type": "record",
     "namespace": "com.example.avro",
     "name": "GameEvent",
     "fields": [
       { "name": "eventId", "type": "string",  "doc": "Unique Id representing event"},
       { "name": "eventTime", "type": "long", "doc": "Time of the event" },
       { "name": "username", "type": "string", "doc": "Player name" },
       { "name": "ipv4", "type": "string", "doc": "Ip Address of the origin server" },
       { "name": "country", "type": "string", "doc": "Country of event origin" },
       { "name": "score", "type": "int", "doc": "Represents game score" },
       { "name": "completed", "type": "boolean", "doc": "Indicates completion of game" }
     ]
}
```
** Schema should match with Avro 1.8 Specifications.

#### Write to BigQuery
  * Output table must already exists and table schema should match schema supplied to generate fake records.
  * Supply following additional parameters:
    - --sinkType=BIGQUERY
    - --outputTableSpec=projectid:datasetid.tableid
  * Optional parameters are:
    - --writeDisposition=[WRITE_APPEND|WRITE_TRUNCATE|WRITE_EMPTY]. Default is WRITE_APPEND
    - --outputDeadletterTable=projectid:datasetid.tableid. If not supplied creates a table with name outputtableid_error_records

Template uses Streaming Inserts method instead of load to write data to BigQuery. Streaming Inserts are not free and subject to quota limits.
For more latest informatioon check [BigQuery docs](https://cloud.google.com/bigquery/streaming-data-into-bigquery)

#### Write to Google Cloud Storage
  * Output Bucket must already exists.
  * Supply following additional parameters:
    - --sinkType=GCS
    - --outputDirectory=gs://bucketname/prefix/
    - --outputType=[AVRO|PQRQUET]. If not specified default output is JSON.
    - --avroSchemaLocation=gs://bucketname/prefix/filename.avsc (Mandatory when Output encoding type is AVRO or PARQUET)
  * Optional parameters include:
    - --windowDuration=< Duration of fixed window >. Default is 1m (i.e 1 minute)
    - --outputFilenamePrefix=< Prefix for each file >. Default is output-
    - --numShards=< Number of output files per window >. Must be specified as 1 or higher number

#### Writing fixed number of records
By default templates generates unlimited number of messages but however if you need fixed number of messages include
the option --messagesLimit=<number> to convert the pipeline from unbounded source to bounded source.


### Building/Using Template

For instructions on how to use or customize, check the template specific
generated documentation:

- [Streaming Data Generator Template](./README_Streaming_Data_Generator.md)

