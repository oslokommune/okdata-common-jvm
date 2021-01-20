package no.ok.origo.dataplatform.commons.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import java.io.PrintWriter
import java.io.StringWriter
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

abstract class DataplatformLoggingRequestHandler<I, O> : RequestHandler<I, O> {

    var dataplatformLogger = DataplatformLogger(LoggerFactory.getLogger(this::class.java))

    private fun flushLog(level: Level = Level.INFO) {
        dataplatformLogger.flushLog(level)
    }

    private fun logRequestContext(context: Context) {
        dataplatformLogger.logAdd(
                "aws_request_id" to context.awsRequestId,
                "function_name" to context.functionName,
                "memory_limit_in_mb" to context.memoryLimitInMB,
                "remaining_time_in_millis" to context.remainingTimeInMillis,
                "service_name" to System.getenv("SERVICE_NAME")
        )
    }

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
        logRequestContext(context)
        runCatching { handleRequestWithLogging(input, context) }.fold(
                onFailure = {
                    val sw = StringWriter()
                    it.printStackTrace(PrintWriter(sw))
                    logAdd("stacktrace" to sw.toString())
                    logAdd("exception" to it.message.toString())
                    logAdd("exception_name" to (it::class.java::getSimpleName)())
                    flushLog(level = Level.ERROR)
                    throw it
                },
                onSuccess = {
                    flushLog(level = Level.INFO)
                    return it
                }
        )
    }

    abstract fun handleRequestWithLogging(inputEvent: I, context: Context): O
}
