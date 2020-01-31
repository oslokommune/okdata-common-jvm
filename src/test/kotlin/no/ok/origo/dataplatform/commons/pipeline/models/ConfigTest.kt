package no.ok.origo.dataplatform.commons.pipeline.models

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

class ConfigTest : AnnotationSpec() {

    lateinit var config: Config
    val raw_json = this::class.java.getResource("/input_event.json").readText()
    val om = jacksonObjectMapper()

    @Test
    fun `test getIntermediatePrefix`() {
        config = om.readValue(raw_json)
        val json = om.readTree(raw_json)
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
        val input = om.readTree(raw_json)
        (input.get("payload").get("pipeline").get("task_config") as ObjectNode).remove(input.get("task").textValue())
        config = om.readValue(om.writeValueAsString(input))
        config.getTaskConfig()
    }
}
