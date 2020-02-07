package no.ok.origo.dataplatform.commons.auth

import no.ok.origo.dataplatform.commons.loggerFor

class ClientCredentialsProvider(
    val clientId: String,
    val clientSecret: String,
    val client: KeycloakClient
) {
    private val logger = loggerFor(javaClass)

    private var _token: AuthToken
    init {
        _token = newToken()
    }
    val token: AuthToken
        get() {
            return when {
                _token.accessTokenValid() -> _token
                _token.refreshTokenValid() -> {
                    _token = refreshToken()
                    return _token
                }
                else -> {
                    _token = newToken()
                    return _token
                }
            }
        }

    fun newToken(): AuthToken {
        val formData = listOf(
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "grant_type" to "client_credentials"
        )
        return client.tokenRequest(parameters = formData)
    }

    fun refreshToken(): AuthToken {
        val formData = listOf(
                "client_id" to clientId,
                "refresh_token" to _token.refreshToken,
                "grant_type" to "refresh_token"
        )
        return client.tokenRequest(parameters = formData)
    }
}
