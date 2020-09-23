package no.ok.origo.dataplatform.commons.lambda

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.event.Level

class DataplatformLogger(val logger: Logger) {

    private val logStatements = mutableMapOf<String, Any>()

    fun addLogStatements(vararg statements: LogEntry) {
        logStatements.putAll(statements)
    }

    fun flushLog(level: Level) {
        val logContent = logStatements.toJson()
        when (level) {
            Level.INFO -> logger.info(logContent)
            Level.ERROR -> logger.error(logContent)
            Level.WARN -> logger.warn(logContent)
            Level.DEBUG -> logger.debug(logContent)
            Level.TRACE -> logger.trace(logContent)
        }
        logStatements.clear()
    }
}

typealias LogEntry = Pair<String, Any>

fun Map<String, Any>.toJson(): String {
    return jacksonObjectMapper().writeValueAsString(this)
}
