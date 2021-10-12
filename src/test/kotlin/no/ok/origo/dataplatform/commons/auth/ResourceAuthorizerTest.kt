package no.ok.origo.dataplatform.commons.auth

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import java.net.URLEncoder

class ResourceAuthorizerTest : AnnotationSpec() {

    var wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().dynamicHttpsPort())

    val keycloakClient = mockk<KeycloakClient>()

    val resourceServerClientId = "some-resource-server"
    val resourceAuthorizer = ResourceAuthorizer(keycloakClient, resourceServerClientId)

    @BeforeEach
    fun startWiremockAndKeycloakMock() {
        wireMockServer.start()
        every {
            keycloakClient.getTokenEndpoint()
        } returns "http://localhost:${wireMockServer.port()}/token"
    }

    @AfterEach
    fun stopWiremock() {
        wireMockServer.resetMappings()
        wireMockServer.stop()
    }

    @Test
    fun `test authorize resource and scope true`() {
        val accessToken = "hdajfladfefdasfasjf"
        val resourceName = "okdata:dataset:some-dataset"
        val scope = "okdata:dataset:read"
        wireMockServer.stubFor(
            post(urlEqualTo("/token"))
                .withHeader("Authorization", equalTo("Bearer $accessToken"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(
                    matching(
                        (
                            "grant_type=${urlEncode("urn:ietf:params:oauth:grant-type:uma-ticket")}&" +
                                "audience=${urlEncode(resourceServerClientId)}&" +
                                "response_mode=decision&" +
                                "permission=${urlEncode("$resourceName#$scope")}"
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody("""{"result": true}""")
                )
        )

        resourceAuthorizer.isAuthorized(accessToken, scope, resourceName) shouldBe true
    }

    @Test
    fun `test authorize scope only true`() {
        val accessToken = "hdajfladfefdasfasjf"
        val scope = "okdata:dataset:read"
        wireMockServer.stubFor(
            post(urlEqualTo("/token"))
                .withHeader("Authorization", equalTo("Bearer $accessToken"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(
                    matching(
                        (
                            "grant_type=${urlEncode("urn:ietf:params:oauth:grant-type:uma-ticket")}&" +
                                "audience=${urlEncode(resourceServerClientId)}&" +
                                "response_mode=decision&" +
                                "permission=${urlEncode("#$scope")}"
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody("""{"result": true}""")
                )
        )

        resourceAuthorizer.isAuthorized(accessToken, scope) shouldBe true
    }

    @Test
    fun `test authorize resource and scope false`() {
        val accessToken = "hdajfladfefdasfasjf"
        val resourceName = "okdata:dataset:some-dataset"
        val scope = "okdata:dataset:read"
        wireMockServer.stubFor(
            post(urlEqualTo("/token"))
                .withHeader("Authorization", equalTo("Bearer $accessToken"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(
                    matching(
                        (
                            "grant_type=${urlEncode("urn:ietf:params:oauth:grant-type:uma-ticket")}&" +
                                "audience=${urlEncode(resourceServerClientId)}&" +
                                "response_mode=decision&" +
                                "permission=${urlEncode("$resourceName#$scope")}"
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(403)
                        .withBody("""{"error": "access_denied","error_description": "not_authorized"}""")
                )
        )

        resourceAuthorizer.isAuthorized(accessToken, scope, resourceName) shouldBe false
    }

    @Test
    fun `test authorize resource and scope whitelist override true`() {
        val accessToken = "hdajfladfefdasfasjf"
        val resourceName = "okdata:dataset:some-dataset"
        val scope = "okdata:dataset:read"
        wireMockServer.stubFor(
            post(urlEqualTo("/token"))
                .withHeader("Authorization", equalTo("Bearer $accessToken"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(
                    matching(
                        (
                            "grant_type=${urlEncode("urn:ietf:params:oauth:grant-type:uma-ticket")}&" +
                                "audience=${urlEncode(resourceServerClientId)}&" +
                                "response_mode=decision&" +
                                "permission=${urlEncode("$resourceName#$scope")}"
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(403)
                        .withBody("""{"error": "access_denied","error_description": "not_authorized"}""")
                )
        )

        wireMockServer.stubFor(
            post(urlEqualTo("/token"))
                .withHeader("Authorization", equalTo("Bearer $accessToken"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(
                    matching(
                        (
                            "grant_type=${urlEncode("urn:ietf:params:oauth:grant-type:uma-ticket")}&" +
                                "audience=${urlEncode(resourceServerClientId)}&" +
                                "response_mode=decision&" +
                                "permission=${urlEncode("#okdata:dataset:whitelist")}"
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody("""{"result": true}""")
                )
        )

        resourceAuthorizer.isAuthorized(accessToken, scope, resourceName, useWhitelist = true) shouldBe true
    }

    @Test
    fun `test authorize resource and scope whitelist override false`() {
        val accessToken = "hdajfladfefdasfasjf"
        val resourceName = "okdata:dataset:some-dataset"
        val scope = "okdata:dataset:read"
        wireMockServer.stubFor(
            post(urlEqualTo("/token"))
                .withHeader("Authorization", equalTo("Bearer $accessToken"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(
                    matching(
                        (
                            "grant_type=${urlEncode("urn:ietf:params:oauth:grant-type:uma-ticket")}&" +
                                "audience=${urlEncode(resourceServerClientId)}&" +
                                "response_mode=decision&" +
                                "permission=${urlEncode("$resourceName#$scope")}"
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(403)
                        .withBody("""{"error": "access_denied","error_description": "not_authorized"}""")
                )
        )

        wireMockServer.stubFor(
            post(urlEqualTo("/token"))
                .withHeader("Authorization", equalTo("Bearer $accessToken"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(
                    matching(
                        (
                            "grant_type=${urlEncode("urn:ietf:params:oauth:grant-type:uma-ticket")}&" +
                                "audience=${urlEncode(resourceServerClientId)}&" +
                                "response_mode=decision&" +
                                "permission=${urlEncode("#okdata:dataset:whitelist")}"
                            )
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(403)
                        .withBody("""{"error": "access_denied","error_description": "not_authorized"}""")
                )
        )

        resourceAuthorizer.isAuthorized(accessToken, scope, resourceName, useWhitelist = true) shouldBe false
    }

    fun urlEncode(parameterValue: String): String {
        return URLEncoder.encode(parameterValue, "utf-8")
    }
}
