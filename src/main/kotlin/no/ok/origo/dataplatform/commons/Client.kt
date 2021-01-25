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
                val responseBody = response.body().asString(contentType = "application/json")
                val customErrorMsg = "url: ${preparedRequest.url}\nresponse body: $responseBody"
                when (statusCode) {
                    400 -> throw BadRequestError(customErrorMsg)
                    404 -> throw NotFoundError(customErrorMsg)
                    500 -> throw ServerError(customErrorMsg)
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
}

class NotFoundError(message: String) : Exception(message)

class ServerError(message: String) : Exception(message)

class BadRequestError(message: String) : Exception(message)
