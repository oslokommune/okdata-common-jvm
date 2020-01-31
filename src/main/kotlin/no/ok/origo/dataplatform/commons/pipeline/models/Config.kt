package no.ok.origo.dataplatform.commons.pipeline.models

import com.fasterxml.jackson.databind.JsonNode

data class Config(
    val execution_name: String,
    val task: String,
    val payload: Payload
) {
    fun getTaskConfig(): JsonNode {
        return this.payload.pipeline.task_config.get(this.task) ?: throw MissingStepConfig()
    }

    fun getIntermediatePrefix(): String {
        return payload.output_dataset.s3_prefix.replace("%stage%", "intermediate") + "$task/"
    }
}

data class Payload(
    val pipeline: Pipeline,
    val output_dataset: OutputDataset,
    val step_data: StepData
)

data class Pipeline(
    val id: String,
    val task_config: JsonNode
)

data class OutputDataset(
    val id: String,
    val version: String,
    val edition: String?,
    val s3_prefix: String
)

data class StepData(
    var s3_input_prefixes: Map<String, String>,
    var status: String,
    var errors: List<String>
)

class MissingStepConfig : Exception("Missing step config for current pipeline step")
