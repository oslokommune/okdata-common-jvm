package no.ok.origo.dataplatform.commons.auth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class AuthTokenTest : AnnotationSpec() {
    val om = jacksonObjectMapper()

    @Test
    fun accessTokenValid() {
        val valid = this::class.java.getResource("/keycloak/auth_token.json").readText()
        val token = om.readValue<AuthToken>(valid)
        token.accessTokenValid() shouldBe true
        val invalid = token.copy(expiresIn = 0)
        invalid.accessTokenValid() shouldBe false
    }

    @Test
    fun refreshTokenValid() {
        val valid = this::class.java.getResource("/keycloak/auth_token.json").readText()
        val token = om.readValue<AuthToken>(valid)
        token.refreshTokenValid() shouldBe true
        val invalid = token.copy(refreshExpiresIn = 0)
        invalid.refreshTokenValid() shouldBe false
    }

    @Test
    fun handleNoRefreshToken() {
        val valid = this::class.java.getResource("/keycloak/auth_token_without_refresh.json").readText()
        val token = om.readValue<AuthToken>(valid)
        token.accessTokenValid() shouldBe true
        token.refreshTokenValid() shouldBe false
    }
}
