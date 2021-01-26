package no.ok.origo.dataplatform.commons.event

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.Fuel
import kotlin.text.Charsets.UTF_8
import no.ok.origo.dataplatform.commons.AuthorizedClient
import no.ok.origo.dataplatform.commons.DataplatformClient
import no.ok.origo.dataplatform.commons.auth.ClientCredentialsProvider
import no.ok.origo.dataplatform.commons.ensureLast
import no.ok.origo.dataplatform.commons.readValue

class EventCollectorClient(
    private val eventCollectorBaseUrl: String = System.getenv("EVENT_COLLECTOR_URL") ?: System.getProperty("EVENT_COLLECTOR_URL")
        ?: throw Exception("Missing Environment: EVENT_COLLECTOR_URL"),
    private val clientCredentialsProvider: ClientCredentialsProvider,
    private val eventBodyObjectMapper: ObjectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
) : AuthorizedClient, DataplatformClient() {

    override fun getToken() = clientCredentialsProvider.token

    fun <T> postEvents(jsonEventList: List<T>, datasetId: String, version: String): EventCollectorResponse {

        if (jsonEventList.isEmpty()) return EventCollectorResponse("Empty event list, skipping post")

        val request = Fuel.post(eventCollectorBaseUrl.ensureLast('/') + "events/$datasetId/$version")
                .body(eventBodyObjectMapper.writeValueAsString(jsonEventList), charset = UTF_8)

        return performRequest(request).readValue(om)
    }
}
