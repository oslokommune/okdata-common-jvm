package no.ok.origo.dataplatform.commons

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Request
import org.slf4j.LoggerFactory

fun <T> loggerFor(clazz: Class<T>) = LoggerFactory.getLogger(clazz)

inline fun <reified T> ByteArray.readValue(om: ObjectMapper): T {
    return om.readValue(this)
}

fun Request.withHeaders(headers: List<Pair<String, String>>?): Request {
    return if (headers == null) {
        this
    } else {
        this.header(headers.toMap())
    }
}
