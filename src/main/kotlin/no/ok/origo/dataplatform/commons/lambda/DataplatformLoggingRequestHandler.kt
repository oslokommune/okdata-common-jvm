package no.ok.origo.dataplatform.commons.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZonedDateTime

abstract class DataplatformLoggingRequestHandler<I, O> : RequestHandler<I, O> {

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

    override fun handleRequest(input: I, context: Context): O {
        dataplatformLogger.logRequestContext(context)
        val startTime = ZonedDateTime.now()
        runCatching { handleRequestWithLogging(input, context) }.fold(
            onFailure = {
                val sw = StringWriter()
                it.printStackTrace(PrintWriter(sw))
                logAdd("stacktrace" to sw.toString())
                logAdd("exception" to it.message.toString())
                logAdd("exception_name" to (it::class.java::getSimpleName)())
                dataplatformLogger.flushLog(Level.ERROR, startTime)
                throw it
            },
            onSuccess = {
                dataplatformLogger.flushLog(Level.INFO, startTime)
                return it
            }
        )
    }

    abstract fun handleRequestWithLogging(inputEvent: I, context: Context): O
}
