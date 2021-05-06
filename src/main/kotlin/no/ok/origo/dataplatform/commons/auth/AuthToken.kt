package no.ok.origo.dataplatform.commons.auth
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AuthToken(
    val accessToken: String,
    val expiresIn: Int,
    val refreshExpiresIn: Int?,
    val refreshToken: String?,
    val tokenType: String,
    @JsonProperty("not-before-policy") val notBeforePolicy: Long,
    val sessionState: String?,
    val scope: String
) {
    var accessTokenExpireTimestamp: Long
    var refreshTokenExpireTimestamp: Long?

    init {
        val now = System.currentTimeMillis()
        accessTokenExpireTimestamp = (now + expiresIn * 1000) - TOKEN_TIME_DRIFT
        refreshTokenExpireTimestamp =
            if (refreshExpiresIn != null) (now + refreshExpiresIn * 1000) - TOKEN_TIME_DRIFT else null
    }
    fun accessTokenValid(): Boolean {
        return accessTokenExpireTimestamp > System.currentTimeMillis()
    }

    fun refreshTokenValid(): Boolean {
        val ts = refreshTokenExpireTimestamp
        return if (ts != null) ts > System.currentTimeMillis() else false
    }
}

val TOKEN_TIME_DRIFT = 10000
