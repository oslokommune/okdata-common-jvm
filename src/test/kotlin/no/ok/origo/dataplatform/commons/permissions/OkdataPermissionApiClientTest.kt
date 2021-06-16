package no.ok.origo.dataplatform.commons.permissions

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.AnnotationSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.ok.origo.dataplatform.commons.ServerError
import no.ok.origo.dataplatform.commons.StandardResponse
import no.ok.origo.dataplatform.commons.TestUtils
import no.ok.origo.dataplatform.commons.auth.AuthToken
import no.ok.origo.dataplatform.commons.auth.ClientCredentialsProvider

class OkdataPermissionApiClientTest : AnnotationSpec() {

    var wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().dynamicHttpsPort())

    private val clientCredentialsProviderMock = mockk<ClientCredentialsProvider>()

    private val authToken = TestUtils.readJson<AuthToken>("keycloak/auth_token.json")

    lateinit var okdataPermissionApiClient: OkdataPermissionApiClient

    @BeforeEach
    fun startWiremock() {
        wireMockServer.start()
        okdataPermissionApiClient = OkdataPermissionApiClient(
            "http://localhost:${wireMockServer.port()}",
            clientCredentialsProviderMock
        )
    }

    @BeforeEach
    fun mockAuthUtils() {
        every {
            clientCredentialsProviderMock.token
        } returns authToken
    }

    @AfterEach
    fun stopWiremock() {
        wireMockServer.resetMappings()
        wireMockServer.stop()
    }

    @AfterEach
    fun clearMock() {
        clearMocks(clientCredentialsProviderMock)
    }

    @Test
    fun `test removeTeamPermissions ok`() {
        val teamName = "some-team"
        val route = "/remove_team_permissions/$teamName"
        wireMockServer.stubFor(
            put(urlEqualTo(route))
                .withHeader("Authorization", equalTo("bearer ${authToken.accessToken}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody("""{"message": "Ok"}""")
                )
        )

        val responseObject = okdataPermissionApiClient.removeTeamPermissions(teamName)

        responseObject shouldBe StandardResponse("Ok")
    }

    @Test
    fun `test removeTeamPermissions failed`() {
        val teamName = "some-team"
        val route = "/remove_team_permissions/$teamName"
        wireMockServer.stubFor(
            put(urlEqualTo(route))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withBody("""{"message": "Server error"}""")
                )
        )

        shouldThrow<ServerError> { okdataPermissionApiClient.removeTeamPermissions(teamName) }
    }
}
