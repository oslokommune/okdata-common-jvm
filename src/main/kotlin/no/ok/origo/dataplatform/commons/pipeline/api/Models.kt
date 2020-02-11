package no.ok.origo.dataplatform.commons.pipeline.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class Pipeline(
    val arn: String,
    val template: String? = null,
    @JsonProperty("transformation_schema") val transformationSchema: String
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
    val pipelineArn: String,
    val schemaId: String?,
    val transformation: JsonNode?,
    val taskConfig: JsonNode? = null,
    val useLatestEdition: Boolean
) {
    fun dataset() = datasetUri.split("/")[1]
    fun version() = datasetUri.split("/")[2]
}