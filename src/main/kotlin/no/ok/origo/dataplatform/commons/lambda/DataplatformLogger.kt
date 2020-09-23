package no.ok.origo.dataplatform.commons.lambda


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.event.Level

class DataplatformLogger(val logger: Logger) {

    private val logStatements = mutableListOf<LogEntry>()

    fun addLogStatements(vararg statements: LogEntry) {
        logStatements.addAll(statements)
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
    }
}

typealias LogEntry = Pair<String, String>

fun List<LogEntry>.toJson(): String {
    return jacksonObjectMapper().writeValueAsString(this)
}