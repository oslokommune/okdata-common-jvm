package no.ok.origo.dataplatform.commons.pipeline.config

import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
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
        config.payload.outputDataset.s3Prefix shouldNotBe null
        config.payload.outputDataset.edition shouldNotBe null

        config.payload.stepData.s3InputPrefixes shouldNotBe null
        config.payload.stepData.inputEvents shouldBe null
    }

    @Test
    fun `Test deserialization json input StepConfig`() {
        val rawJson = this::class.java.getResource("/pipeline.config/json_input_config.json").readText()
        val config = om.readValue<Config>(rawJson)

        config.payload.outputDataset.s3Prefix shouldBe null
        config.payload.outputDataset.edition shouldBe null

        config.payload.stepData.s3InputPrefixes shouldBe null
        config.payload.stepData.inputEvents shouldNotBe null
    }

    @Test(expected = ValueInstantiationException::class)
    fun `Test deserialization fails if both s3_input_prefixes and input_events are null`() {
        val rawJson = this::class.java.getResource("/pipeline.config/no_step_data_input_config.json").readText()
        val config = om.readValue<Config>(rawJson)
    }

    @Test(expected = ValueInstantiationException::class)
    fun `Test deserialization fails if both s3_input_prefixes and input_events are not null`() {
        val rawJson = this::class.java.getResource("/pipeline.config/json_input_and_s3_input_config.json").readText()
        val config = om.readValue<Config>(rawJson)
    }
}
