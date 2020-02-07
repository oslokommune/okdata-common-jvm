package no.ok.origo.dataplatform.commons.auth

import com.fasterxml.jackson.databind.JsonNode
import com.github.kittinunf.fuel.Fuel
import no.ok.origo.dataplatform.commons.DataplatformClient
import no.ok.origo.dataplatform.commons.readValue

class KeycloakClient(val server: String, val realm: String) : DataplatformClient() {
    var tokenEndpoint: String
    init {
        tokenEndpoint = wellKnownConfiguration().get("token_endpoint").textValue()
    }

    fun wellKnownConfiguration(): JsonNode {
        val request = Fuel.get(path = server + "auth/realms/$realm/.well-known/openid-configuration")
        return performRequest(request).readValue(om)
    }

    fun tokenRequest(parameters: List<Pair<String, String>>): AuthToken {
        val request = Fuel.post(path = tokenEndpoint, parameters = parameters)
        return performRequest(request).readValue(om)
    }
}
