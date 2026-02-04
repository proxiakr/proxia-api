package kr.proxia.client.oauth.github

data class GitHubProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
)
