package no.ok.origo.dataplatform.commons

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import no.ok.origo.dataplatform.commons.auth.AuthToken

interface AuthorizedClient {
    fun getToken(): AuthToken
}

abstract class DataplatformClient {
    private val logger = loggerFor(this::class.java)
    val om = jacksonObjectMapper()

    fun performRequest(request: Request): ByteArray {
        addAuthorizationHeader(request)
        val (preparedRequest, response, result) = request.response()

        return when (result) {
            is Result.Success -> result.get()
            is Result.Failure -> {
                logger.debug("Request: " +
                        "url: ${preparedRequest.url} " +
                        "body: ${preparedRequest.body} "
                )

                logger.debug("Response: " +
                        "url: ${response.url} " +
                        "status: ${response.statusCode} ",
                        "body: ${response.responseMessage} "
                        )
                throw result.getException()
            }
        }
    }

    private fun addAuthorizationHeader(request: Request): Request {
        if (this is AuthorizedClient) {
            val token = getToken()
            request.appendHeader(Headers.AUTHORIZATION, "${token.tokenType} ${token.accessToken}")
        }
        return request
    }
}
