package kr.proxia.client.oauth.github

data class GitHubTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val scope: String,
)
