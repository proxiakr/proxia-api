package kr.proxia.client.git.github

data class GitHubRepositoriesResponse(
    val repositories: List<GitHubRepository>,
    val hasNext: Boolean,
)
