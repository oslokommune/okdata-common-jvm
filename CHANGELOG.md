## ?.?.?

* The `PipelineInstance` data class now longer accepts the `schemaId` and `useLatestEdition` parameters

## 0.0.20

* Made `DataplatformLogger` instance public

## 0.0.19

* Added logging for `lambda.runtime.RequestHandler`

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
