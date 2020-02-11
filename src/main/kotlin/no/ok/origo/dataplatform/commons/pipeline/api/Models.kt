package no.ok.origo.dataplatform.commons.pipeline.api

import com.fasterxml.jackson.databind.JsonNode

data class Pipeline(
        val arn: String,
        val template: String? = null,
        val transformation_schema: String
)

data class PipelineInput(
        val pipelineInstanceId: String,
        val datasetUri: String,
        val stage: String
)

data class PipelineInstance(
        val id: String,
        val datasetUri: String,
        val pipelineArn: String,
        val schemaId: String?,
        val transformation: JsonNode?,
        val taskConfig: JsonNode? = null,
        val useLatestEdition: Boolean
)
