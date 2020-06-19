package no.ok.origo.dataplatform.commons.pipeline.config.events

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.shouldThrow
import io.kotlintest.specs.AnnotationSpec

class ModelsTest : AnnotationSpec() {

    val om = jacksonObjectMapper()

    @Test
    fun `Test deserialization and object structure`() {
        val rawJson = this::class.java.getResource("/pipeline.config.events/pipeline_config_events_data_input.json").readText()
        shouldNotThrowAny {
            om.readValue<Config>(rawJson)
        }
    }

    @Test
    fun `Test throws MissingStepConfig`() {
        val rawJsonMissingStepConfig = this::class.java.getResource("/pipeline.config.events/pipeline_config_events_data_input_missing_step_config.json").readText()
        shouldThrow<MissingStepConfig> {
            om.readValue<Config>(rawJsonMissingStepConfig).getTaskConfig()
        }
    }
}
