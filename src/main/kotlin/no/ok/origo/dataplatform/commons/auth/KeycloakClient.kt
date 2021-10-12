package no.ok.origo.dataplatform.commons.auth

import com.fasterxml.jackson.databind.JsonNode
import com.github.kittinunf.fuel.Fuel
import no.ok.origo.dataplatform.commons.DataplatformClient
import no.ok.origo.dataplatform.commons.ensureLast
import no.ok.origo.dataplatform.commons.readValue

class KeycloakClient(val server: String, val realm: String) : DataplatformClient() {
    private lateinit var tokenEndpoint: String

    fun getTokenEndpoint(): String {
        if (!this::tokenEndpoint.isInitialized) {
            tokenEndpoint = wellKnownConfiguration().get("token_endpoint").textValue()
        }
        return tokenEndpoint
    }

    fun wellKnownConfiguration(): JsonNode {
        val request = Fuel.get(path = server.ensureLast('/') + "auth/realms/$realm/.well-known/openid-configuration")
        return performRequest(request).readValue(om)
    }

    fun tokenRequest(parameters: List<Pair<String, String>>): AuthToken {
        val request = Fuel.post(path = getTokenEndpoint(), parameters = parameters)
        return performRequest(request).readValue(om)
    }
}
