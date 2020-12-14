package no.ok.origo.dataplatform.commons.lambda

import com.amazonaws.services.lambda.runtime.Context
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

internal class DataplatformHandler : DataplatformLoggingHandler() {

    fun somethingElse(): String {
        return "Hello World"
    }

    fun final(@Suppress("UNUSED_PARAMETER") context: Context) {
        logAdd("final" to "hello again")
        println("Hello again")
    }

    fun logStuffAndHandleRequest(context: TestContext, stuffToLog: List<LogEntry>) {
        val out = ByteArrayOutputStream()
        stuffToLog.forEach {
            logAdd(it) }
        handleRequest("".byteInputStream(), out, context)
    }

    override fun handleRequestWithLogging(input: InputStream, output: OutputStream, context: Context) {
        logAdd("user", "ID-123")
        somethingElse()
        final(context)
    }
}

internal class DataplatformRequestHandler : DataplatformLoggingRequestHandler<LambdaEvent, Unit>() {

    fun somethingElse(): String {
        return "Hello World"
    }

    fun final(@Suppress("UNUSED_PARAMETER") context: Context) {
        logAdd("final" to "hello again")
        println("Hello again")
    }

    fun logStuffAndHandleRequest(context: TestContext, stuffToLog: List<LogEntry>) {
        stuffToLog.forEach {
            logAdd(it) }
        handleRequest(LambdaEvent("foo", "bar"), context)
    }

    override fun handleRequestWithLogging(inputEvent: LambdaEvent, context: Context) {
        logAdd("user", "ID-123")
        somethingElse()
        final(context)
    }
}

class ExpectedException : Exception("Expected exception in test")
internal class DataplatformHandlerThrowsException : DataplatformLoggingHandler() {

    fun somethingElse(): String {
        throw ExpectedException()
    }

    fun final(@Suppress("UNUSED_PARAMETER") context: Context) {
        logAdd("final" to "hello again")
        println("Hello again")
    }

    override fun handleRequestWithLogging(input: InputStream, output: OutputStream, context: Context) {
        logAdd("user", "ID-123")
        somethingElse()
        final(context)
    }
}

data class LambdaEvent(
    val foo: String,
    val bar: String
)
