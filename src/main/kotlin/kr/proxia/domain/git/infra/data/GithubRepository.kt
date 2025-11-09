package kr.proxia.domain.git.infra.data

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubRepository(
    val id: Long,
    val name: String,
    @field:JsonProperty("full_name")
    val fullName: String,
    val private: Boolean,
    val url: String,
)
