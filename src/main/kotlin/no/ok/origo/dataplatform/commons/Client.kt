package no.ok.origo.dataplatform.commons

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import no.ok.origo.dataplatform.commons.auth.AuthToken
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URL
import java.util.function.Supplier

interface AuthorizedClient {
    fun getToken(): AuthToken
}

abstract class DataplatformClient {
    private val logger = loggerFor(this::class.java)
    open val om = jacksonObjectMapper()

    val MAX_RETRIES = 3

    private val intervalFunction = IntervalFunction
        .ofExponentialRandomBackoff()

    // https://github.com/resilience4j/resilience4j/issues/758
    private val retryConfig = RetryConfig.custom<RetryConfig>()
        .maxAttempts(MAX_RETRIES)
        .intervalFunction(intervalFunction)
        .retryExceptions(ServerError::class.java)
        .build()

    fun performRequest(request: Request): ByteArray {
        val registry = RetryRegistry.of(retryConfig)
        val retry = registry.retry("execute")

        val executeWithRetry: Supplier<ByteArray> = Retry.decorateSupplier(retry) { execute(request) }

        return executeWithRetry.get()
    }

    fun execute(request: Request): ByteArray {
        addAuthorizationHeader(request)
        val (preparedRequest, response, result) = request.response()

        return when (result) {
            is Result.Success -> result.get()
            is Result.Failure -> {
                logger.debug(
                    "Request: " +
                        "url: ${preparedRequest.url} " +
                        "body: ${preparedRequest.body} "
                )

                logger.debug(
                    "Response: " +
                        "url: ${response.url} " +
                        "status: ${response.statusCode} ",
                    "body: ${response.responseMessage} "
                )
                val fuelError = result.getException()
                val exception = fuelError.exception
                val statusCode = fuelError.response.statusCode
                val rawResponseBody = response.body().asString(contentType = "application/json")
                val url = preparedRequest.url
                when (statusCode) {
                    400 -> {
                        val responseBody = StandardResponse.fromRawJson(rawResponseBody, "Bad request")
                        throw BadRequestError(customErrorMsg(statusCode, responseBody, url), exception)
                    }

                    404 -> {
                        val responseBody = StandardResponse.fromRawJson(rawResponseBody, "Not found")
                        throw NotFoundError(customErrorMsg(statusCode, responseBody, url), exception)
                    }

                    500, 502, 503, 504 -> {
                        val responseBody = StandardResponse.fromRawJson(rawResponseBody, "Server error")
                        throw ServerError(customErrorMsg(statusCode, responseBody, url), exception)
                    }

                    else -> when (exception) {
                        is SocketException, is SocketTimeoutException ->
                            throw ServerError(customErrorMsg(statusCode, url), exception)
                        else -> throw UnforseenError(customErrorMsg(statusCode, url), exception)
                    }
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

    private fun customErrorMsg(statusCode: Int, responseBody: StandardResponse, url: URL): String {
        return "url: $url\nstatusCode: $statusCode\nmessage: ${responseBody.message}"
    }

    private fun customErrorMsg(statusCode: Int, url: URL): String {
        return "url: $url\nstatusCode: $statusCode"
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

class NotFoundError(message: String, cause: Throwable) : RuntimeException(message, cause)

class ServerError(message: String, cause: Throwable) : RuntimeException(message, cause)

class BadRequestError(message: String, cause: Throwable) : RuntimeException(message, cause)

class UnforseenError(message: String, cause: Throwable) : RuntimeException(message, cause)
