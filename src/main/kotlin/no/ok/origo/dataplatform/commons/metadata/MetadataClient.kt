package no.ok.origo.dataplatform.commons.metadata

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import no.ok.origo.dataplatform.commons.DataplatformClient
import no.ok.origo.dataplatform.commons.readValue
import no.ok.origo.dataplatform.commons.withHeaders

class MetadataClient(var api: String) : DataplatformClient() {
    override val om = jacksonObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)

    fun datasetPath(datasetid: String? = null) =
            listOfNotNull("datasets", datasetid)
                    .joinToString("/")

    fun versionPath(datasetid: String, version: String? = null) =
            listOfNotNull(datasetPath(datasetid), "versions", version)
                    .joinToString("/")

    fun editionPath(datasetid: String, version: String, edition: String? = null) =
            listOfNotNull(versionPath(datasetid, version), "editions", edition)
                    .joinToString("/")

    fun distributionPath(datasetid: String, version: String, edition: String, distribution: String? = null) =
            listOfNotNull(editionPath(datasetid, version, edition), "distribution", distribution)
                    .joinToString("/")

    fun get(path: String, queryParams: List<Pair<String, String>>? = null): ByteArray {
        val url = api + path
        val request = Fuel.get(path = url, parameters = queryParams)
        return performRequest(request)
    }

    fun post(path: String, body: String): ByteArray {
        val request = "$api$path".httpPost().jsonBody(body)
        return performRequest(request)
    }

    fun post(path: String, body: Any, headers: List<Pair<String, String>>?): ByteArray {
        val request = "$api$path".httpPost().body(om.writeValueAsBytes(body)).withHeaders(headers)
        return performRequest(request)
    }

    fun listDatasets(): List<Dataset> {
        return get(datasetPath()).readValue(om)
    }

    fun getDataset(datasetid: String): Dataset {
        return get(datasetPath(datasetid)).readValue(om)
    }

    fun listVersions(datasetid: String): List<Version> {
        return get(versionPath(datasetid)).readValue(om)
    }

    fun Dataset.listVersions(): List<Version> {
        return listVersions(datasetid = Id)
    }

    fun getVersion(datasetid: String, version: String): Version {
        return get(versionPath(datasetid, version)).readValue(om)
    }

    fun Dataset.getVersion(version: String): Version {
        return getVersion(datasetid = Id, version = version)
    }

    fun listEditions(datasetid: String, version: String): List<Edition> {
        return get(editionPath(datasetid, version)).readValue(om)
    }

    fun Version.listEditions(): List<Edition> {
        val (datasetid, version) = Id.split("/")
        return listEditions(datasetid, version)
    }

    fun getEdition(datasetid: String, version: String, edition: String): Edition {
        return get(editionPath(datasetid, version, edition)).readValue(om)
    }

    fun Version.getEdition(edition: String): Edition {
        val (datasetid, version) = Id.split("/")
        return getEdition(datasetid = datasetid, version = version, edition = edition)
    }

    fun listDistributions(datasetid: String, version: String, edition: String): List<Distribution> {
        return get(distributionPath(datasetid, version, edition)).readValue(om)
    }

    fun Edition.listDistributions(): List<Distribution> {
        val (datasetid, version, edition) = Id.split("/")
        return listDistributions(datasetid = datasetid, version = version, edition = edition)
    }

    fun getDistribution(datasetid: String, version: String, edition: String, distribution: String): Distribution {
        return get(distributionPath(datasetid, version, edition, distribution)).readValue(om)
    }

    fun Edition.getDistribution(distribution: String): Distribution {
        val (datasetid, version, edition) = Id.split("/")
        return getDistribution(datasetid, version, edition, distribution)
    }
}
