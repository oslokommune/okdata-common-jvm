package no.ok.origo.dataplatform.commons

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object TestUtils {
    val mapper = jacksonObjectMapper()

    inline fun <reified T : Any> readJson(filename: String): T {
        val jsonString = readTestResource(filename)
        return mapper.readValue(jsonString, T::class.java)
    }

    fun readTestResource(filename: String): String {
        return this::class.java.classLoader.getResource("$filename").readText()
    }
}
