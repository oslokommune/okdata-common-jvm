package no.ok.origo.dataplatform.commons.pipeline.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class Pipeline(
    val arn: String,
    val template: String? = null,
    @JsonProperty("transformation_schema") val transformationSchema: JsonNode?,
    @JsonProperty("task_config") val taskConfig: JsonNode?
)

data class PipelineInput(
    val pipelineInstanceId: String,
    val datasetUri: String,
    val stage: String
) {
    fun dataset() = datasetUri.split("/")[1]
    fun version() = datasetUri.split("/")[2]
}

data class PipelineInstance(
    val id: String,
    val datasetUri: String,
    // TODO: Remove this once all users have been updated to use
    // `pipelineProcessorId` instead.
    val pipelineArn: String = "",
    // TODO: Make this required once `pipelineArn` has been phased out.
    var pipelineProcessorId: String = "",
    val schemaId: String?,
    val transformation: JsonNode?,
    val taskConfig: JsonNode? = null,
    val useLatestEdition: Boolean
) {
    fun dataset() = datasetUri.split("/")[1]
    fun version() = datasetUri.split("/")[2]
}
