package no.ok.origo.dataplatform.commons.lambda

import com.amazonaws.services.lambda.runtime.Context
import java.io.InputStream
import java.io.OutputStream

internal class DataplatformHandler : DataplatformLoggingHandler() {

    fun somethingElse(): String {
        return "Hello World"
    }

    fun final(context: Context) {
        logAdd("final" to "hello again")
        println("Hello again")
    }

    override fun handleRequestWithLogging(input: InputStream, output: OutputStream, context: Context) {
        logAdd("user", "ID-123")
        val result = somethingElse()
        final(context)
    }
}

class ExpectedException : Exception("Expected exception in test")
internal class DataplatformHandlerThrowsException : DataplatformLoggingHandler() {

    fun somethingElse(): String {
        throw ExpectedException()
    }

    fun final(context: Context) {
        logAdd("final" to "hello again")
        println("Hello again")
    }

    override fun handleRequestWithLogging(input: InputStream, output: OutputStream, context: Context) {
        logAdd("user", "ID-123")
        val result = somethingElse()
        final(context)
    }
}
