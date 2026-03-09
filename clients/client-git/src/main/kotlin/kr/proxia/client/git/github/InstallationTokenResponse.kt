package kr.proxia.client.git.github

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class InstallationTokenResponse(
    val token: String,
    @JsonProperty("expires_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    val expiresAt: LocalDateTime,
    val permissions: Map<String, String>? = null,
    @JsonProperty("repository_selection")
    val repositorySelection: String? = null,
)
