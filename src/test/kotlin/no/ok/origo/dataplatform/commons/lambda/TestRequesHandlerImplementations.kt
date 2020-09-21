package no.ok.origo.dataplatform.commons.lambda

import com.amazonaws.services.lambda.runtime.Context
import java.io.InputStream
import java.io.OutputStream

internal class DataplatformLoggingHandler : DataplatformRequestStreamHandler() {

    fun somethingElse(): String {
        return "Hello World"
    }

    fun final(context: Context) {
        context.logAdd("final" to "hello again")
        println("Hello again")
    }

    override fun handleRequestWithLogging(input: InputStream, output: OutputStream, context: Context) {
        context.logAdd("user", "ID-123")
        val result = somethingElse()
        final(context)
    }
}

class ExpectedException : Exception("Expected exception in test")
internal class DataplatformLoggingHandlerThrowsException : DataplatformRequestStreamHandler() {

    fun somethingElse(): String {
        throw ExpectedException()
    }

    fun final(context: Context) {
        context.logAdd("final" to "hello again")
        println("Hello again")
    }

    override fun handleRequestWithLogging(input: InputStream, output: OutputStream, context: Context) {
        context.logAdd("user", "ID-123")
        val result = somethingElse()
        final(context)
    }
}
