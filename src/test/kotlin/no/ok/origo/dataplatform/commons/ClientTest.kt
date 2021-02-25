package no.ok.origo.dataplatform.commons

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.AnnotationSpec
import io.mockk.spyk
import io.mockk.verify

class ClientTest : AnnotationSpec() {

    class TestClient : DataplatformClient()

    val testClient = TestClient()
    val testRoute = "/test"
    var wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().dynamicHttpsPort())

    @BeforeEach
    fun startWiremock() {
        wireMockServer.start()
    }

    @AfterEach
    fun stopWiremock() {
        wireMockServer.resetMappings()
        wireMockServer.stop()
    }

    @Test
    fun `test raises ServerError`() {

        wireMockServer.stubFor(
            get(urlEqualTo(testRoute))
                .willReturn(
                    aResponse().withStatus(500)
                        .withBody("""{"message": "Server error"}""")
                )

        )
        val testUrl = "http://localhost:${wireMockServer.port()}" + testRoute
        val testRequest = spyk(Fuel.get(testUrl))

        val error = shouldThrow<ServerError> { testClient.performRequest(testRequest) }

        error.message shouldBe "url: $testUrl\nstatusCode: 500\nmessage: Server error"
        verify(exactly = testClient.MAX_RETRIES) { testRequest.response() }
    }

    @Test
    fun `test raises BadRequestError`() {

        wireMockServer.stubFor(
            get(urlEqualTo(testRoute))
                .willReturn(
                    aResponse().withStatus(400)
                        .withBody("""{"message": "Bad request"}""")
                )

        )
        val testUrl = "http://localhost:${wireMockServer.port()}" + testRoute
        val testRequest = spyk(Fuel.get(testUrl))

        val error = shouldThrow<BadRequestError> { testClient.performRequest(testRequest) }

        error.message shouldBe "url: $testUrl\nstatusCode: 400\nmessage: Bad request"
        verify(exactly = 1) { testRequest.response() }
    }

    @Test
    fun `test raises NotFoundError`() {

        wireMockServer.stubFor(
            get(urlEqualTo(testRoute))
                .willReturn(
                    aResponse().withStatus(404)
                        .withBody("""{"message": "Not found"}""")
                )

        )
        val testUrl = "http://localhost:${wireMockServer.port()}" + testRoute
        val testRequest = spyk(Fuel.get(testUrl))

        val error = shouldThrow<NotFoundError> { testClient.performRequest(testRequest) }

        error.message shouldBe "url: $testUrl\nstatusCode: 404\nmessage: Not found"
        verify(exactly = 1) { testRequest.response() }
    }

    @Test
    fun `test raises UnforseenError`() {

        wireMockServer.stubFor(
            get(urlEqualTo(testRoute))
                .willReturn(
                    aResponse().withStatus(418)
                        .withBody("I'm a teapot")
                )

        )
        val testUrl = "http://localhost:${wireMockServer.port()}" + testRoute
        val testRequest = spyk(Fuel.get(testUrl))

        val error = shouldThrow<UnforseenError> { testClient.performRequest(testRequest) }

        error.message shouldBe "url: $testUrl\nstatusCode: 418"
        assert(error.cause is FuelError)
        verify(exactly = 1) { testRequest.response() }
    }
}
