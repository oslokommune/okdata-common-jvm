package no.ok.origo.dataplatform.commons.metadata

import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldThrow
import io.kotlintest.specs.AnnotationSpec

class ModelsTest : AnnotationSpec() {

    val om = jacksonObjectMapper()

    @Test
    fun `test deserialize Distribution`() {

        val rawJsonWithFilename =
            """{"Id": "some-id", "Type": "Distribution", "filename": "file.csv", "_links": {"foo": "bar"}}"""
        val rawJsonWithFilenames =
            """{"Id": "some-id", "Type": "Distribution", "filenames": ["file.csv"], "_links": {"foo": "bar"}}"""

        val distributionWithFilename = om.readValue<Distribution>(rawJsonWithFilename)
        val distributionWithFilenames = om.readValue<Distribution>(rawJsonWithFilenames)

        assert(distributionWithFilename.filename == "file.csv")
        assert(distributionWithFilename.filenames == null)

        assert(distributionWithFilenames.filenames == listOf("file.csv"))
        assert(distributionWithFilenames.filename == null)
    }

    @Test
    fun `test Distribution filename and filenames requirements`() {
        val rawJsonFilenameAndFilenamesNull = """{"Id": "some-id", "Type": "Distribution", "_links": {"foo": "bar"}}"""
        val rawJsonFilenameAndFilenamesNotNull =
            """{"Id": "some-id", "Type": "Distribution", "filename": "file.csv", "filenames": ["file.csv"], "_links": {"foo": "bar"}}"""

        shouldThrow<ValueInstantiationException> {
            om.readValue<Distribution>(rawJsonFilenameAndFilenamesNull)
        }
        shouldThrow<ValueInstantiationException> {
            om.readValue<Distribution>(rawJsonFilenameAndFilenamesNotNull)
        }
    }
}
