## 0.4.2

* Include log level in log events

## 0.4.1

* Log timestamp with correct format

## 0.4.0

* Added logging of timestamp and duration for Lambdas.
* Upgraded to Kotlin 1.4.

## 0.3.0

* Added a new `source.type` field to the `Dataset` class.

## 0.2.0

* Added a new `state` field to the `Dataset` class.

## 0.1.0

* Add optional field "filenames" to metadat Distribution and make field "filename" optional

## 0.0.26

* Return custom exceptions with info about url-path and response message for failed http-requests.

## 0.0.25

* The dataset `Dataset.confidentiality` field is now optional.

## 0.0.24

* Log stacktrace when exceptions occur.

## 0.0.23

* Add support for default and missing task configuration values in pipelines.

## 0.0.22

* Keeps the `PipelineInstance` data class backwards compatible with the previous
  change.

## 0.0.21

* The `PipelineInstance` data class now longer accepts the `schemaId`,
  `transformation`, and `useLatestEdition` parameters.

## 0.0.20

* Made `DataplatformLogger` instance public.

## 0.0.19

* Added logging for `lambda.runtime.RequestHandler`.

## 0.0.18

* Upgrade to Gradle 6.6.1
* The `PipelineInstance` data class now takes `pipelineProcessorId` as an optional parameter

## 0.0.16 (and 0.0.17)

* Add abstract class for common logging in lambda handlers

## 0.0.15

* Exclude null fields in pipeline model from JSON serialization

## 0.0.14

* Publish with correct JDK version

## 0.0.13

* Add support for event data in pipeline model

## 0.0.12

* Add auth header to Metadata client

## 0.0.11

* Support new pipeline configuration format (`task_config`)

## 0.0.10

* Improvements to pipeline API client

## 0.0.9

* Fix auth token refresh requests

## 0.0.8

* Fix missing trailing `/` in request URLs

## 0.0.7

* Add metadata API client
* Add event collector client

## 0.0.4

* Add Keycloak authentication
* Add pipeline API client

## 0.0.3

* Initial release
* Pipeline configuration classes
