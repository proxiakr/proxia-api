package kr.proxia.client.git.github

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubRepositoriesResponse(
    @JsonProperty("total_count")
    val totalCount: Int,
    val repositories: List<GitHubRepository>
)
