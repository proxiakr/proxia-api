package kr.proxia.client.git.github

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubRepository(
    val id: Long,
    val name: String,
    @JsonProperty("full_name")
    val fullName: String,
    @JsonProperty("default_branch")
    val defaultBranch: String,
    val private: Boolean,
    @JsonProperty("html_url")
    val htmlUrl: String,
)
