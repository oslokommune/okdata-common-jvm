## 0.7.0

* Library is now released on jitpack.io
* New uri for dependency: com.github.oslokommune:okdata-common-jvm:X.Y.Z

## 0.6.2

* KeycloakClientCredentialsProvider no longer does a http call to token endpoint in constructor

## 0.6.1

* OkdataPermissionApiClient is now an open class
* Keycloak client no longer does a http call to well known endpoint in constructor

## 0.6.0

* Add OkdataPermissionApiClient with method for removing team permissions

## 0.5.5

* Log raw response bodies on unforeseen errors

## 0.5.4

* Handle tokens without refresh token (from Keycloak v12)

## 0.5.3

* Increase token time drift to 10 seconds
* Fix refresh of Keycloak access token when refresh token is invalid, e.g.
  due to inactive session because Keycloak server restarted.

## 0.5.2

* Allow null values in log entries

## 0.5.1

* Rename ResourceAuthorizer.is_authorized -> ResourceAuthorizer.isAuthorized
* Add flag for whitelist overriding in ResourceAuthorizer.isAuthorized

## 0.5.0

* Add client ResourceAuthorizer for authorizing resource access with keycloak

## 0.4.7

* Add retries on socket timeout

## 0.4.6

* Add logging of cold start state for Lambda functions
* Fix logging of remaining time for Lambda functions

## 0.4.5

* Add retries on SocketException

## 0.4.4

* Added http status code to error messages
* Added custom UnforseenError

## 0.4.3

* Add retries to http calls from clients

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
