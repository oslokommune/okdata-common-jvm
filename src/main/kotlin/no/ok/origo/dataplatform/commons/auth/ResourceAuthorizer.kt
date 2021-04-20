package no.ok.origo.dataplatform.commons.auth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result

class ResourceAuthorizer(
    private val keycloakClient: KeycloakClient,
    private val resourceServerClientId: String
) {

    private val objectMapper = jacksonObjectMapper()

    fun is_authorized(accessToken: String, scope: String, resource: String? = null): Boolean {

        val authParameters = listOf(
            "grant_type" to "urn:ietf:params:oauth:grant-type:uma-ticket",
            "audience" to resourceServerClientId,
            "response_mode" to "decision",
            "permission" to "${resource ?: ""}#$scope"
        )
        val authorizationRequest = Fuel.post(keycloakClient.tokenEndpoint, authParameters)
            .header(
                mapOf(
                    "Authorization" to "Bearer $accessToken",
                    "Content-Type" to "application/x-www-form-urlencoded"
                )
            )

        val (request, response, result) = authorizationRequest.response()
        return when (result) {
            is Result.Success ->
                objectMapper
                    .readValue<Decision>(response.body().toByteArray())
                    .result
            is Result.Failure -> {
                val fuelError = result.getException()
                if (fuelError.response.statusCode == 403) return false
                throw fuelError
            }
        }
    }

    data class Decision(
        val result: Boolean
    )
}
