package no.ok.origo.dataplatform.commons.pipeline

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import no.ok.origo.dataplatform.commons.TestUtils
import no.ok.origo.dataplatform.commons.auth.ClientCredentialsProvider
import no.ok.origo.dataplatform.commons.pipeline.api.Pipeline
import no.ok.origo.dataplatform.commons.pipeline.api.PipelineInput
import no.ok.origo.dataplatform.commons.pipeline.api.PipelineInstance


internal class PipelineClientTest: AnnotationSpec() {

    lateinit var client : PipelineClient
    val om = jacksonObjectMapper()
    val pipeline = TestUtils.readJson<Pipeline>("pipeline/api/pipeline.json")
    val pipelineInstance = TestUtils.readJson<PipelineInstance>("pipeline/api/pipeline-instance.json")
    val pipelineInput = TestUtils.readJson<PipelineInput>("pipeline/api/pipeline-input.json")

    @BeforeEach
    fun beforeEach(){
        val credentials = mockk<ClientCredentialsProvider>(relaxed = true)
        client = spyk(PipelineClient(
                credentialsProvider = credentials,
                baseUrl = "https://mock/")
        )
    }

    @AfterEach
    fun afterEach(){
        clearAllMocks()
    }

    @Test
    fun pipelineInstancePath() {
        client.pipelineInstancePath() shouldBe "pipeline-instances"
    }

    @Test
    fun pipelineInputPath() {
        client.pipelineInputPath("pipeline-instance-id") shouldBe "pipeline-instances/pipeline-instance-id/inputs"
    }

    @Test
    fun getPipeline() {
        val path = slot<String>()
        every { client.get(path=capture(path), queryParams = null)} returns  om.writeValueAsBytes(pipeline)

        client.getPipeline(pipeline.arn) shouldBe pipeline
        path.captured shouldBe "pipelines/${pipeline.arn}"

    }

    @Test
    fun getPipelines() {
        val path = slot<String>()
        every { client.get(path=capture(path), queryParams = null)} returns  om.writeValueAsBytes(listOf(pipeline))

        client.getPipelines() shouldBe listOf(pipeline)
        path.captured shouldBe "pipelines"
    }

    @Test
    fun getPipelineInstance() {
        val path = slot<String>()
        every { client.get(path=capture(path), queryParams = null)} returns  om.writeValueAsBytes(pipelineInstance)

        client.getPipelineInstance("pipeline-instance-id") shouldBe pipelineInstance
        path.captured shouldBe "pipeline-instances/${pipelineInstance.id}"
    }

    @Test
    fun getPipelineInstances() {
        val path = slot<String>()
        every { client.get(path=capture(path), queryParams = null)} returns  om.writeValueAsBytes(listOf(pipelineInstance))

        client.getPipelineInstances() shouldBe listOf(pipelineInstance)
        path.captured shouldBe "pipeline-instances"
    }

    @Test
    fun getPipelineInputs() {
        val path = slot<String>()
        every { client.get(path=capture(path), queryParams = null)} returns  om.writeValueAsBytes(listOf(pipelineInput))

        client.getPipelineInputs(pipelineInput.pipelineInstanceId) shouldBe listOf(pipelineInput)
        path.captured shouldBe "pipeline-instances/${pipelineInput.pipelineInstanceId}/inputs"
    }

    @Test
    fun getPipelineInstancesFromInput() {
        val path = slot<String>()
        val params = slot<List<Pair<String,String>>>()

        every { client.get(path=capture(path), queryParams = capture(params))} returns  om.writeValueAsBytes(listOf(pipelineInstance))

        val (_, inputDatasetId, inputVersion) = pipelineInput.datasetUri.split("/")

        client.getPipelineInstances(
                inputDatasetId = inputDatasetId,
                inputDatasetVersion = inputVersion,
                inputDatasetStage = pipelineInput.stage
        ) shouldBe listOf(pipelineInstance)

        path.captured shouldBe "pipeline-instances"
        params.captured shouldBe listOf("datasetid" to inputDatasetId, "version" to inputVersion, "stage" to pipelineInput.stage)
    }

    @Test
    fun pipelineInstanceExtensionTest(){
        val path = slot<String>()
        every { client.get(path=capture(path), queryParams = null)} returns  om.writeValueAsBytes(listOf(pipelineInput))

        client.run {
            pipelineInstance.getPipelineInputs() shouldBe listOf(pipelineInput)
        }
        path.captured shouldBe "pipeline-instances/${pipelineInput.pipelineInstanceId}/inputs"
    }
}