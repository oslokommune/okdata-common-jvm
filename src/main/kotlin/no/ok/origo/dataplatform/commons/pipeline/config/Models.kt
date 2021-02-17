package no.ok.origo.dataplatform.commons.pipeline.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Config(
    val executionName: String,
    val task: String,
    val payload: Payload,
    val taskConfig: ObjectNode = JsonNodeFactory.instance.objectNode()
) {
    fun getTaskConfig(): JsonNode {
        // Get global task config from pipeline template
        val taskConfig = this.taskConfig

        val pipelineTaskConfigs = this.payload.pipeline.taskConfig
        val pipelineTaskConfig: JsonNode? = pipelineTaskConfigs.get(this.task)

        if (pipelineTaskConfig == null || pipelineTaskConfig is NullNode) {
            return taskConfig
        }

        // Update with specific config from pipeline instance
        return taskConfig.setAll(pipelineTaskConfig as ObjectNode)
    }

    fun getIntermediatePrefix(): String {
        return payload.outputDataset.s3Prefix?.replace("%stage%", "intermediate") + "$task/"
    }

    init {
        if (payload.stepData.s3InputPrefixes == null) {
            require(payload.stepData.inputEvents != null)
        } else {
            require(payload.outputDataset.s3Prefix != null)
            require(payload.stepData.inputEvents == null)
        }
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Payload(
    val pipeline: Pipeline,
    val outputDataset: OutputDataset,
    val stepData: StepData
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Pipeline(
    val id: String,
    val taskConfig: JsonNode = JsonNodeFactory.instance.objectNode()
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OutputDataset(
    val id: String,
    val version: String,
    val edition: String?,
    val s3Prefix: String?
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class StepData(
    var inputEvents: List<JsonNode>?,
    var s3InputPrefixes: Map<String, String>?,
    var status: String,
    var errors: List<String>
)
