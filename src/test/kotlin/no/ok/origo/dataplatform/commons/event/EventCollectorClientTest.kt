package no.ok.origo.dataplatform.commons.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.AnnotationSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.ok.origo.dataplatform.commons.NotFoundError
import no.ok.origo.dataplatform.commons.TestUtils
import no.ok.origo.dataplatform.commons.auth.AuthToken
import no.ok.origo.dataplatform.commons.auth.ClientCredentialsProvider

class EventCollectorClientTest : AnnotationSpec() {

    var wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().dynamicHttpsPort())

    private val clientCredentialsProviderMock = mockk<ClientCredentialsProvider>()

    private val authToken = TestUtils.readJson<AuthToken>("keycloak/auth_token.json")

    lateinit var eventCollectorClient: EventCollectorClient

    @BeforeEach
    fun startWiremock() {
        wireMockServer.start()
        eventCollectorClient = EventCollectorClient(
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
    fun `test postEvents ok`() {
        val datasetId = "test-dataset"
        val version = "test-version"
        val route = "/events/$datasetId/$version"
        val testObject = TestEvent("foo", "bar")
        val eventList = listOf(testObject, testObject)
        wireMockServer.stubFor(
            post(urlEqualTo(route))
                .withHeader("Authorization", equalTo("bearer ${authToken.accessToken}"))
                .withRequestBody(equalTo(jacksonObjectMapper().writeValueAsString(eventList)))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody("""{"message": "Ok"}""")
                )
        )

        val responseObject = eventCollectorClient.postEvents(eventList, datasetId, version)

        responseObject shouldBe EventCollectorResponse("Ok")
    }

    @Test
    fun `test postEvents fail`() {

        val datasetId = "test-dataset"
        val version = "test-version"
        val route = "/events/$datasetId/$version"
        val responseBody = """{"message": "Custom msg"}"""
        wireMockServer.stubFor(
            post(urlEqualTo(route))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withBody(responseBody)
                )
        )

        val testObject = TestEvent("foo", "bar")
        val li = listOf(testObject, testObject)

        val exeption = shouldThrow<NotFoundError> {
            eventCollectorClient.postEvents(li, datasetId, version)
        }
        exeption.localizedMessage shouldBe "url: http://localhost:${wireMockServer.port()}/events/$datasetId/$version\n" +
            "statusCode: 404\nmessage: Custom msg"
    }

    @Test
    fun `test postEvents empty list`() {
        val response = eventCollectorClient.postEvents(emptyList<TestEvent>(), "datasetId", "version")

        response shouldBe EventCollectorResponse("Empty event list, skipping post")
    }
}

data class TestEvent(
    val foo: String,
    val bar: String
)
