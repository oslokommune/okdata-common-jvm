package no.ok.origo.dataplatform.commons.metadata

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.every
import io.mockk.spyk
import io.mockk.unmockkAll
import java.net.URL

class MetadataClientTest : AnnotationSpec() {
    inline fun <reified T : Any> URL.parse(): T {
        return om.readValue(this.readText())
    }

    lateinit var client: MetadataClient
    val om = jacksonObjectMapper()
    val dataset = this::class.java.getResource("/metadata/dataset.json").parse<Dataset>()
    val version = this::class.java.getResource("/metadata/version.json").parse<Version>()
    val edition = this::class.java.getResource("/metadata/edition.json").parse<Edition>()
    val distribution = this::class.java.getResource("/metadata/distribution.json").parse<Distribution>()

    @BeforeEach
    fun beforeEach() {
        client = spyk(MetadataClient("http://example.com/"))
    }

    @AfterEach
    fun afterEach() {
        unmockkAll()
    }

    @Test
    fun datasetPath() {
        val (datasetid, _) = distribution.Id.split("/")

        val listDatasetPath = client.datasetPath()
        val getDatasetPath = client.datasetPath(datasetid)
        listDatasetPath shouldBe "datasets"
        getDatasetPath shouldBe "datasets/$datasetid"

        every { client.get(getDatasetPath) } returns om.writeValueAsBytes(dataset)
        every { client.get(listDatasetPath) } returns om.writeValueAsBytes(listOf(dataset))

        client.getDataset(datasetid) shouldBe dataset
        client.listDatasets() shouldBe listOf(dataset)
    }

    @Test
    fun getVersion() {
        val (datasetid, versionid, _) = distribution.Id.split("/")

        val listVersionsPath = client.versionPath(datasetid)
        val getVersionPath = client.versionPath(datasetid, versionid)
        listVersionsPath shouldBe "datasets/$datasetid/versions"
        getVersionPath shouldBe "datasets/$datasetid/versions/$versionid"

        every { client.get(getVersionPath) } returns om.writeValueAsBytes(version)
        every { client.get(listVersionsPath) } returns om.writeValueAsBytes(listOf(version))

        client.run {
            dataset.listVersions() shouldBe listOf(version)
            dataset.getVersion(versionid) shouldBe version
        }
    }

    @Test
    fun getEdition() {
        val (datasetid, versionid, editionid, _) = distribution.Id.split("/")

        val listEditionsPath = client.editionPath(datasetid, versionid)
        val getEditionPath = client.editionPath(datasetid, versionid, editionid)
        listEditionsPath shouldBe "datasets/$datasetid/versions/$versionid/editions"
        getEditionPath shouldBe "datasets/$datasetid/versions/$versionid/editions/$editionid"

        every { client.get(getEditionPath) } returns om.writeValueAsBytes(edition)
        every { client.get(listEditionsPath) } returns om.writeValueAsBytes(listOf(edition))

        client.run {
            version.listEditions() shouldBe listOf(edition)
            version.getEdition(editionid) shouldBe edition
        }
    }

    @Test
    fun getDistribution() {
        val (datasetid, versionid, editionid, distributionid) = distribution.Id.split("/")
        val listDistributionsPath = client.distributionPath(datasetid, versionid, editionid)
        val getDistributionsPath = client.distributionPath(datasetid, versionid, editionid, distributionid)
        listDistributionsPath shouldBe "datasets/$datasetid/versions/$versionid/editions/$editionid/distribution"
        getDistributionsPath shouldBe "datasets/$datasetid/versions/$versionid/editions/$editionid/distribution/$distributionid"

        every { client.get(getDistributionsPath) } returns om.writeValueAsBytes(distribution)
        every { client.get(listDistributionsPath) } returns om.writeValueAsBytes(listOf(distribution))

        client.run {
            edition.listDistributions() shouldBe listOf(distribution)
            edition.getDistribution(distributionid) shouldBe distribution
        }
    }
}
