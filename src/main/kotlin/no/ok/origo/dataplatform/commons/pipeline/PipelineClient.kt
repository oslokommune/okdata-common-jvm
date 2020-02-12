package no.ok.origo.dataplatform.commons.pipeline

import com.github.kittinunf.fuel.Fuel
import no.ok.origo.dataplatform.commons.AuthorizedClient
import no.ok.origo.dataplatform.commons.DataplatformClient
import no.ok.origo.dataplatform.commons.auth.ClientCredentialsProvider
import no.ok.origo.dataplatform.commons.ensureLast
import no.ok.origo.dataplatform.commons.pipeline.api.Pipeline
import no.ok.origo.dataplatform.commons.pipeline.api.PipelineInput
import no.ok.origo.dataplatform.commons.pipeline.api.PipelineInstance
import no.ok.origo.dataplatform.commons.readValue

class PipelineClient(
    private val credentialsProvider: ClientCredentialsProvider,
    var baseUrl: String
) : AuthorizedClient, DataplatformClient() {
    override fun getToken() = credentialsProvider.token

    fun get(path: String, queryParams: List<Pair<String, String>>? = null): ByteArray {
        val url = baseUrl.ensureLast('/') + path
        val request = Fuel.get(path = url, parameters = queryParams)
        return performRequest(request)
    }

    fun pipelinePath() = "pipelines"
    fun pipelineInstancePath() = "pipeline-instances"
    fun pipelineInputPath(pipelineInstanceId: String) = "pipeline-instances/$pipelineInstanceId/inputs"

    fun getPipeline(arn: String): Pipeline {
        return get(pipelinePath() + "/$arn").readValue(om)
    }

    fun getPipelines(): List<Pipeline> {
        return get(pipelinePath()).readValue(om)
    }

    fun getPipelineInstance(id: String): PipelineInstance {
        return get(pipelineInstancePath() + "/$id").readValue(om)
    }

    fun getPipelineInstances(inputDatasetId: String, inputDatasetVersion: String, inputDatasetStage: String?): List<PipelineInstance> {
        val params = if (inputDatasetStage == null) {
            listOf("dataset-id" to inputDatasetId, "version" to inputDatasetVersion)
        } else {
            listOf("dataset-id" to inputDatasetId, "version" to inputDatasetVersion, "stage" to inputDatasetStage)
        }
        return get(pipelineInstancePath(), params).readValue(om)
    }

    fun getPipelineInstances(): List<PipelineInstance> {
        return get(pipelineInstancePath()).readValue(om)
    }

    fun getPipelineInputs(pipelineInstanceId: String): List<PipelineInput> {
        return get(pipelineInputPath(pipelineInstanceId)).readValue(om)
    }

    fun PipelineInstance.getPipelineInputs(): List<PipelineInput> {
        return getPipelineInputs(id)
    }
}
