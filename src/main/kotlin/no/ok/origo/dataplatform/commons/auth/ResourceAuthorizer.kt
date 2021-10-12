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

    fun isAuthorized(accessToken: String, scope: String, resource: String? = null, useWhitelist: Boolean = false): Boolean {

        val authParameters = listOf(
            "grant_type" to "urn:ietf:params:oauth:grant-type:uma-ticket",
            "audience" to resourceServerClientId,
            "response_mode" to "decision",
            "permission" to "${resource ?: ""}#$scope"
        )
        val authorizationRequest = Fuel.post(keycloakClient.getTokenEndpoint(), authParameters)
            .header(
                mapOf(
                    "Authorization" to "Bearer $accessToken",
                    "Content-Type" to "application/x-www-form-urlencoded"
                )
            )

        val (request, response, result) = authorizationRequest.response()
        val hasAccess = when (result) {
            is Result.Success ->
                objectMapper
                    .readValue<Decision>(response.body().toByteArray())
                    .result
            is Result.Failure -> {
                when (result.getException().response.statusCode) {
                    403 -> false
                    else ->
                        throw result.getException()
                }
            }
        }

        if (!hasAccess and useWhitelist) {
            return isAuthorized(accessToken, "okdata:dataset:whitelist", useWhitelist = false)
        }

        return hasAccess
    }

    data class Decision(
        val result: Boolean
    )
}
