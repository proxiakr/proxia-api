package kr.proxia.client.git.github

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubRepositoriesApiResponse(
    @JsonProperty("total_count")
    val totalCount: Int,
    @JsonProperty("repositories")
    val repositories: List<GitHubRepository>,
)
