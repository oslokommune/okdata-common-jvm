package no.ok.origo.dataplatform.commons

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import java.net.URL
import no.ok.origo.dataplatform.commons.auth.AuthToken

interface AuthorizedClient {
    fun getToken(): AuthToken
}

abstract class DataplatformClient {
    private val logger = loggerFor(this::class.java)
    open val om = jacksonObjectMapper()

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
                val exception = result.getException()
                val statusCode = exception.response.statusCode
                val rawResponseBody = response.body().asString(contentType = "application/json")
                val url = preparedRequest.url
                when (statusCode) {
                    400 -> {
                        val responseBody = StandardResponse.fromRawJson(rawResponseBody, "Bad request")
                        throw BadRequestError(customErrorMsg(responseBody, url))
                    }

                    404 -> {
                        val responseBody = StandardResponse.fromRawJson(rawResponseBody, "Not found")
                        throw NotFoundError(customErrorMsg(responseBody, url))
                    }

                    500 -> {
                        val responseBody = StandardResponse.fromRawJson(rawResponseBody, "Server error")
                        throw ServerError(customErrorMsg(responseBody, url))
                    }

                    else -> throw exception
                }
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

    private fun customErrorMsg(response: StandardResponse, url: URL): String {
        return "url: $url\nmessage: ${response.message}"
    }
}

data class StandardResponse(
    val message: String
) {

    companion object {
        fun fromRawJson(json: String, fallback_message: String): StandardResponse {
            try {
                return jacksonObjectMapper().readValue<StandardResponse>(json)
            } catch (e: Exception) {
                return StandardResponse(fallback_message)
            }
        }
    }
}

class NotFoundError(message: String) : Exception(message)

class ServerError(message: String) : Exception(message)

class BadRequestError(message: String) : Exception(message)
