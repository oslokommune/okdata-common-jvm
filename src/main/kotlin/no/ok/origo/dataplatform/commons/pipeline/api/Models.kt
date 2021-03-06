package no.ok.origo.dataplatform.commons.pipeline.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class PipelineInstance(
    val id: String,
    val datasetUri: String,
    // TODO: Remove this once all users have been updated to use
    // `pipelineProcessorId` instead.
    val pipelineArn: String = "",
    // TODO: Make this required once `pipelineArn` has been phased out.
    var pipelineProcessorId: String = "",
    val taskConfig: JsonNode? = null
) {
    fun dataset() = datasetUri.split("/")[1]
    fun version() = datasetUri.split("/")[2]
}
