package no.ok.origo.dataplatform.commons.event

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlin.text.Charsets.UTF_8
import no.ok.origo.dataplatform.commons.auth.ClientCredentialsProvider

class EventCollectorClient(
    private val baseUrl: String = System.getenv("EVENT_COLLECTOR_URL") ?: System.getProperty("EVENT_COLLECTOR_URL")
        ?: throw Exception("Missing Environment: EVENT_COLLECTOR_URL"),
    private val clientCredentialsProvider: ClientCredentialsProvider,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
) {

    fun <T> postEvents(jsonEventList: List<T>, datasetId: String, version: String): EventCollectorResponse {

        if (jsonEventList.isEmpty()) return EventCollectorResponse("Empty event list, skipping post")

        val (request, response, result) = "$baseUrl/events/$datasetId/$version"
                .httpPost()
                .header(mapOf("Authorization" to "Bearer ${clientCredentialsProvider.token.accessToken}"))
                .body(objectMapper.writeValueAsString(jsonEventList), charset = UTF_8)
                .responseString()

        when (result) {
            is Result.Failure -> {
                val responseObject = objectMapper.readValue(response.data, EventCollectorResponse::class.java)
                throw HttpException(response.statusCode, responseObject.message)
            }
            is Result.Success -> {
                return objectMapper.readValue(result.get(), EventCollectorResponse::class.java)
            }
        }
    }
}
