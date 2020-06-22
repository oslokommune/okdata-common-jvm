package no.ok.origo.dataplatform.commons.pipeline.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

class ModelsTest : AnnotationSpec() {

    val om = jacksonObjectMapper()

    @Test
    fun `test getIntermediatePrefix`() {
        val rawJson = this::class.java.getResource("/pipeline.config/s3_input_config.json").readText()
        val config = om.readValue<Config>(rawJson)
        val json = om.readTree(rawJson)
        val s3_prefix = json.get("payload")
                .get("output_dataset")
                .get("s3_prefix")
                .textValue()
                .replace("%stage%", "intermediate")
                .plus(config.task)
                .plus("/")

        s3_prefix shouldBe config.getIntermediatePrefix()
    }

    @Test(expected = MissingStepConfig::class)
    fun `get task info throws if missing`() {
        val rawJsonMissingStepConfig = this::class.java.getResource("/pipeline.config/missing_step_config.json").readText()
        om.readValue<Config>(rawJsonMissingStepConfig).getTaskConfig()
    }

    @Test
    fun `Test deserialization s3 input StepConfig`() {
        val rawJson = this::class.java.getResource("/pipeline.config/s3_input_config.json").readText()
        val config = om.readValue<Config>(rawJson)
        config.payload.stepData should beInstanceOf<S3InputStepData>()
    }

    @Test
    fun `Test deserialization json input StepConfig`() {
        val rawJson = this::class.java.getResource("/pipeline.config/json_input_config.json").readText()

        val config = om.readValue<Config>(rawJson)

        config.payload.stepData should beInstanceOf<JsonInputStepData>()
    }
}
