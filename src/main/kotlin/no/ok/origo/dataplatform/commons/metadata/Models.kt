package no.ok.origo.dataplatform.commons.metadata
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class Dataset(
    val theme: Any?, // Can be list or string
    @JsonProperty("processing_stage") val processingStage: String?,
    @JsonProperty("parent_id") val parentId: String?,
    val publisher: String,
    val confidentiality: String?,
    val frequency: String?,
    val keywords: List<String>?,
    val objective: String?,
    val contactPoint: ContactPoint,
    val description: String?,
    val accessRights: String?,
    val state: String?,
    val Id: String,
    val Type: String,
    val title: String,
    val source: Source,
    val _links: JsonNode
)

data class Source(
    val type: String
)

data class ContactPoint(
    val email: String,
    val name: String?,
    val phone: String?
)

data class Version(
    val Id: String,
    val version: String,
    val Type: String,
    val _links: JsonNode
)

data class Edition(
    val Id: String,
    val edition: String,
    val Type: String,
    val _links: JsonNode
)

data class Distribution(
    val Id: String,
    val Type: String,
    var filename: String?,
    val filenames: List<String>?,
    val _links: JsonNode
) {
    init {
        when (filename) {
            null -> require(filenames != null)
            else -> require(filenames == null)
        }
        when (filenames) {
            null -> require(filename != null)
            else -> require(filename == null)
        }
    }
}
