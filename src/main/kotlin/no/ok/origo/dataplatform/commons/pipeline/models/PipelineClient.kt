package no.ok.origo.dataplatform.commons.pipeline.models

import com.github.kittinunf.fuel.Fuel
import no.ok.origo.dataplatform.commons.AuthorizedClient
import no.ok.origo.dataplatform.commons.DataplatformClient
import no.ok.origo.dataplatform.commons.auth.ClientCredentialsProvider
import no.ok.origo.dataplatform.commons.readValue

class PipelineClient(
    private val credentialsProvider: ClientCredentialsProvider,
    var api: String = System.getenv("PIPELINE_API") ?: System.getProperty("PIPELINE_API")
    ?: throw Exception("Missing Environment: PIPELINE_API")
) : AuthorizedClient, DataplatformClient() {
    override fun getToken() = credentialsProvider.token

    inline fun <reified T : Any> get(path: String, queryParams: List<Pair<String, String>>? = null): T {
        val request = Fuel.get(api + path, parameters = queryParams)
        return handleResult(request).readValue(om)
    }
}
