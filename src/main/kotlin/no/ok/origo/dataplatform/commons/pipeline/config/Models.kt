package no.ok.origo.dataplatform.commons.pipeline.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Config(
    val executionName: String,
    val task: String,
    val payload: Payload
) {
    fun getTaskConfig(): JsonNode {
        return this.payload.pipeline.taskConfig.get(this.task) ?: throw MissingStepConfig()
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

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Payload(
    val pipeline: Pipeline,
    val outputDataset: OutputDataset,
    val stepData: StepData
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Pipeline(
    val id: String,
    val taskConfig: JsonNode
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class OutputDataset(
    val id: String,
    val version: String,
    val edition: String?,
    val s3Prefix: String?
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class StepData(
    var inputEvents: List<JsonNode>?,
    var s3InputPrefixes: Map<String, String>?,
    var status: String,
    var errors: List<String>
)

class MissingStepConfig : Exception("Missing step config for current pipeline step")
