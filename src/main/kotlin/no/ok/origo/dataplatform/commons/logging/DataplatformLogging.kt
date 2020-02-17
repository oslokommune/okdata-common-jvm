package no.ok.origo.dataplatform.commons.logging

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.InputStream
import java.io.OutputStream

class DataplatformLogger(val name: String){
    companion object {
        private val loggers = mutableMapOf<String, DataplatformLogger>()

        private fun addLogger(logger: DataplatformLogger): DataplatformLogger {
            loggers[logger.name] = logger
            return loggers[logger.name]!!
        }
        fun removeLogger(name: String){
            loggers.remove(name)
        }
        fun getLogger(name: String): DataplatformLogger {
            return loggers[name] ?: addLogger(DataplatformLogger(name))
        }
    }
    private val logStatements = mutableListOf<Pair<String,String>>()

    fun addLogStatements(vararg statements: Pair<String, String>){
        logStatements.addAll(statements)
    }

    fun flushLog(level: Level){
        val logger = LoggerFactory.getLogger(name)
        val logContent = logStatements.toJson()
        when (level) {
            Level.INFO -> logger.info(logContent)
            Level.ERROR -> logger.error(logContent)
            Level.WARN -> logger.warn(logContent)
            Level.DEBUG -> logger.debug(logContent)
            Level.TRACE -> logger.trace(logContent)
        }
        removeLogger(this.name)
    }

    fun List<Pair<String, String>>.toJson(): String {
        return jacksonObjectMapper().writeValueAsString(this)
    }
}

abstract class LoggingRequestStreamHandler : RequestStreamHandler {

    private fun Context.flushLog(level: Level = Level.INFO) {
        DataplatformLogger.getLogger(awsRequestId).flushLog(level)
    }

    private fun Context.newLogger() {
        val logger = DataplatformLogger.getLogger(awsRequestId)
        logger.addLogStatements(
                "awsRequestId" to awsRequestId,
                "functionName" to functionName,
                "memoryLimitInMB" to memoryLimitInMB.toString(),
                "remainingTimeInMillis" to remainingTimeInMillis.toString()
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
                    context.logAdd("Exception" to it.message.toString())
                    context.logAdd("ExceptionName" to (it::class.java::getSimpleName)())
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


