package no.ok.origo.dataplatform.commons.auth

import com.github.kittinunf.fuel.core.HttpException

class ClientCredentialsProvider(
    val clientId: String,
    val clientSecret: String,
    val client: KeycloakClient
) {

    private lateinit var _token: AuthToken

    val token: AuthToken
        get() {
            return when {
                !this::_token.isInitialized -> {
                    _token = newToken()
                    return _token
                }
                _token.accessTokenValid() -> _token
                _token.refreshTokenValid() -> {
                    _token = try {
                        refreshToken()
                    } catch (e: HttpException) {
                        newToken()
                    }
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
            "client_secret" to clientSecret,
            "refresh_token" to _token.refreshToken!!,
            "grant_type" to "refresh_token"
        )
        return client.tokenRequest(parameters = formData)
    }
}
