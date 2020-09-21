package no.ok.origo.dataplatform.commons.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream
import org.slf4j.event.Level

abstract class DataplatformRequestStreamHandler : RequestStreamHandler {

    private fun Context.flushLog(level: Level = Level.INFO) {
        DataplatformLogger.getLogger(awsRequestId).flushLog(level)
    }

    private fun Context.newLogger() {
        val logger = DataplatformLogger.getLogger(awsRequestId)
        logger.addLogStatements(
                "aws_request_id" to awsRequestId,
                "function_name" to functionName,
                "memory_limit_in_mb" to memoryLimitInMB.toString(),
                "remaining_time_in_millis" to remainingTimeInMillis.toString(),
                "service_name" to System.getenv("SERVICE_NAME")
        )
    }

    fun Context.logAdd(key: String, value: String) {
        DataplatformLogger.getLogger(awsRequestId).addLogStatements(key to value)
    }

    fun Context.logAdd(statement: Pair<String, String>) {
        DataplatformLogger.getLogger(awsRequestId).addLogStatements(statement)
    }

    fun Context.logAdd(vararg statements: Pair<String, String>) {
        DataplatformLogger.getLogger(awsRequestId).addLogStatements(*statements)
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        context.newLogger()
        runCatching { handleRequestWithLogging(input, output, context) }.fold(
                onFailure = {
                    context.logAdd("exception" to it.message.toString())
                    context.logAdd("exception_name" to (it::class.java::getSimpleName)())
                    context.flushLog(level = Level.ERROR)
                    throw it
                },
                onSuccess = {
                    context.flushLog(level = Level.INFO)
                }
        )
    }

    abstract fun handleRequestWithLogging(input: InputStream, output: OutputStream, context: Context)
}
