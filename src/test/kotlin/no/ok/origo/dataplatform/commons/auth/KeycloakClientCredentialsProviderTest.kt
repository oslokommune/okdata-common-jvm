package no.ok.origo.dataplatform.commons.auth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.HttpException
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay

internal class KeycloakClientCredentialsProviderTest : AnnotationSpec() {

    lateinit var provider: ClientCredentialsProvider
    lateinit var mockClient: KeycloakClient
    val om = jacksonObjectMapper()

    @BeforeEach
    fun beforeEach() {
        val wellKnown = this::class.java.getResource("/keycloak/well-known.json").readText()
        val authToken = this::class.java.getResource("/keycloak/auth_token.json").readText()
        mockClient = mockk()
        every { mockClient.wellKnownConfiguration() } returns om.readTree(wellKnown)
        every { mockClient.tokenRequest(parameters = any()) } returns om.readValue(authToken)

        provider = ClientCredentialsProvider(
            "client-id",
            clientSecret = "secret-key",
            client = mockClient
        )
    }

    @Test
    fun getToken() {
        provider.token.accessTokenValid() shouldBe true
        provider.token.refreshTokenValid() shouldBe true
    }

    @Test
    fun getExpiredToken() {
        val authToken = om.readValue<AuthToken>(this::class.java.getResource("/keycloak/auth_token.json"))
        val expiredToken: AuthToken = authToken.copy(
            expiresIn = -1
        )
        every { mockClient.tokenRequest(parameters = any()) } returns expiredToken

        val local = provider.newToken()
        local.accessTokenValid() shouldBe false
    }

    @Test
    suspend fun supportShortLivedTokens() {
        val authToken = om.readValue<AuthToken>(this::class.java.getResource("/keycloak/auth_token.json"))
        val expiredToken = authToken.copy(expiresIn = 11)
        every { mockClient.tokenRequest(parameters = any()) } returns expiredToken
        val local = provider.newToken()
        local.accessTokenValid() shouldBe true
        delay(1000)
        local.accessTokenValid() shouldBe false
        provider.token.accessTokenValid() shouldBe true
    }

    @Test
    fun handleInvalidSessionOnRefresh() {
        val authToken = om.readValue<AuthToken>(this::class.java.getResource("/keycloak/auth_token.json"))
        val expiredToken = authToken.copy(expiresIn = 3)

        val exception = HttpException(400, "invalid session")
        every { mockClient.tokenRequest(parameters = any()) } returns expiredToken andThenThrows exception andThen authToken

        // Create provider here to have it initialize with the expired token
        provider = ClientCredentialsProvider(
            "client-id",
            clientSecret = "secret-key",
            client = mockClient
        )

        val local = provider.token
        local.accessTokenValid() shouldBe true
    }
}
