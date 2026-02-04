package kr.proxia.client.oauth.github

data class GitHubEmail(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
)
