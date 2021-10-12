package no.ok.origo.dataplatform.commons.auth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class KeycloakClientTest : AnnotationSpec() {

    var wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().dynamicHttpsPort())
    lateinit var keycloakClient: KeycloakClient
    val realm = "some-realm"
    val om = jacksonObjectMapper()

    lateinit var wellKnownResponse: String
    lateinit var tokenUrl: String
    val authToken = this::class.java.getResource("/keycloak/auth_token.json").readText()

    @BeforeEach
    fun beforeEach() {
        wireMockServer.start()
        val serverUrl = "http://localhost:${wireMockServer.port()}"
        tokenUrl = "$serverUrl/token"
        wellKnownResponse = """{"token_endpoint": "$tokenUrl"}"""
        keycloakClient = KeycloakClient(serverUrl, realm)
    }

    @AfterEach
    fun stopWiremock() {
        wireMockServer.resetMappings()
        wireMockServer.stop()
    }

    @Test
    fun wellKnownConfiguration() {
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlEqualTo("/auth/realms/$realm/.well-known/openid-configuration"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(wellKnownResponse)
                )
        )
        val actual = keycloakClient.wellKnownConfiguration()
        actual shouldBe om.readValue(wellKnownResponse)
    }

    @Test
    fun tokenRequest() {
        val parameters = listOf("cake" to "yum")
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlEqualTo("/auth/realms/$realm/.well-known/openid-configuration"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(wellKnownResponse)
                )
        )
        wireMockServer.stubFor(
            WireMock.post(WireMock.urlEqualTo("/token"))
                .withRequestBody(
                    matching(("cake=yum"))
                )
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(authToken)
                )
        )
        keycloakClient.tokenRequest(parameters) shouldBe om.readValue<AuthToken>(authToken)
        keycloakClient.getTokenEndpoint()shouldBe tokenUrl
    }
}
