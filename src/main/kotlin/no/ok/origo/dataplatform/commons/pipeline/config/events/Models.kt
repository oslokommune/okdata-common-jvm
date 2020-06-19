package no.ok.origo.dataplatform.commons.pipeline.config.events

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
    val version: String
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class StepData(
    var inputEvents: List<JsonNode>,
    var status: String,
    var errors: List<String>
)

class MissingStepConfig : Exception("Missing step config for current pipeline step")
