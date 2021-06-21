package no.ok.origo.dataplatform.commons.permissions

import com.github.kittinunf.fuel.Fuel
import no.ok.origo.dataplatform.commons.AuthorizedClient
import no.ok.origo.dataplatform.commons.DataplatformClient
import no.ok.origo.dataplatform.commons.StandardResponse
import no.ok.origo.dataplatform.commons.auth.ClientCredentialsProvider
import no.ok.origo.dataplatform.commons.readValue

class OkdataPermissionApiClient(
    private val okdataPermissionApiBaseUrl: String,
    private val clientCredentialsProvider: ClientCredentialsProvider,
) : AuthorizedClient, DataplatformClient() {

    override fun getToken() = clientCredentialsProvider.token

    fun removeTeamPermissions(teamName: String): StandardResponse {
        val request = Fuel.put("$okdataPermissionApiBaseUrl/remove_team_permissions/$teamName")
        return performRequest(request).readValue(om)
    }
}
