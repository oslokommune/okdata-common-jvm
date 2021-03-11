package no.ok.origo.dataplatform.commons.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZonedDateTime

abstract class DataplatformLoggingHandler : RequestStreamHandler {

    var dataplatformLogger = DataplatformLogger(LoggerFactory.getLogger(this::class.java))

    fun logAdd(key: String, value: Any) {
        dataplatformLogger.logAdd(key to value)
    }

    fun logAdd(statement: LogEntry) {
        dataplatformLogger.logAdd(statement)
    }

    fun logAdd(vararg statements: LogEntry) {
        dataplatformLogger.logAdd(*statements)
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        dataplatformLogger.logRequestContext(context)
        val startTime = ZonedDateTime.now()
        runCatching { handleRequestWithLogging(input, output, context) }.fold(
            onFailure = {
                val sw = StringWriter()
                it.printStackTrace(PrintWriter(sw))
                logAdd("stacktrace" to sw.toString())
                logAdd("exception" to it.message.toString())
                logAdd("exception_name" to (it::class.java::getSimpleName)())
                dataplatformLogger.flushLog(Level.ERROR, startTime, context)
                throw it
            },
            onSuccess = {
                dataplatformLogger.flushLog(Level.INFO, startTime, context)
            }
        )
    }

    abstract fun handleRequestWithLogging(input: InputStream, output: OutputStream, context: Context)
}
