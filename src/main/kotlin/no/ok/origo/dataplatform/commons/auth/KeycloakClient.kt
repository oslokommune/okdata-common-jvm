package no.ok.origo.dataplatform.commons.auth

import com.fasterxml.jackson.databind.JsonNode
import com.github.kittinunf.fuel.Fuel
import no.ok.origo.dataplatform.commons.DataplatformClient
import no.ok.origo.dataplatform.commons.ensureLast
import no.ok.origo.dataplatform.commons.readValue

class KeycloakClient(val server: String, val realm: String) : DataplatformClient() {
    lateinit var tokenEndpoint: String

    fun wellKnownConfiguration(): JsonNode {
        val request = Fuel.get(path = server.ensureLast('/') + "auth/realms/$realm/.well-known/openid-configuration")
        return performRequest(request).readValue(om)
    }

    fun tokenRequest(parameters: List<Pair<String, String>>): AuthToken {
        if (!this::tokenEndpoint.isInitialized) {
            tokenEndpoint = wellKnownConfiguration().get("token_endpoint").textValue()
        }
        val request = Fuel.post(path = tokenEndpoint, parameters = parameters)
        return performRequest(request).readValue(om)
    }
}
