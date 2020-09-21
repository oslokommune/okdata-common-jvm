package no.ok.origo.dataplatform.commons.lambda

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

class DataplatformLogger(val name: String) {
    companion object {
        private val loggers = mutableMapOf<String, DataplatformLogger>()

        private fun addLogger(logger: DataplatformLogger): DataplatformLogger {
            loggers[logger.name] = logger
            return loggers[logger.name]!!
        }
        fun removeLogger(name: String) {
            loggers.remove(name)
        }
        fun getLogger(name: String): DataplatformLogger {
            return loggers[name] ?: addLogger(DataplatformLogger(name))
        }
    }
    private val logStatements = mutableListOf<Pair<String, String>>()

    fun addLogStatements(vararg statements: Pair<String, String>) {
        logStatements.addAll(statements)
    }

    fun flushLog(level: Level) {
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
