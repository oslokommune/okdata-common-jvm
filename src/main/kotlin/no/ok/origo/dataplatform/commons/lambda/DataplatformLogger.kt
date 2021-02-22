package no.ok.origo.dataplatform.commons.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.event.Level
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DataplatformLogger(val logger: Logger) {

    private val logContent = mutableMapOf<String, Any>()

    fun logAdd(vararg statements: LogEntry) {
        logContent.putAll(statements)
    }

    fun flushLog(level: Level, startTime: ZonedDateTime) {
        val durationMs = calculateDuration(startTime)
        val timestampISO = startTime.withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        logAdd("timestamp" to timestampISO, "duration_ms" to durationMs)
        logAdd("level" to level.name.toLowerCase())
        val logContent = logContent.toJson()
        when (level) {
            Level.INFO -> logger.info(logContent)
            Level.ERROR -> logger.error(logContent)
            Level.WARN -> logger.warn(logContent)
            Level.DEBUG -> logger.debug(logContent)
            Level.TRACE -> logger.trace(logContent)
        }
        this.logContent.clear()
    }

    fun logRequestContext(context: Context) {
        logAdd(
            "aws_request_id" to context.awsRequestId,
            "function_name" to context.functionName,
            "memory_limit_in_mb" to context.memoryLimitInMB,
            "remaining_time_in_millis" to context.remainingTimeInMillis,
            "service_name" to System.getenv("SERVICE_NAME")
        )
    }

    fun calculateDuration(startTime: ZonedDateTime): Long {
        val startTimeMillis = startTime.toInstant().toEpochMilli()
        return ZonedDateTime.now().toInstant().toEpochMilli() - startTimeMillis
    }
}

typealias LogEntry = Pair<String, Any>

fun Map<String, Any>.toJson(): String {
    return jacksonObjectMapper().writeValueAsString(this)
}
