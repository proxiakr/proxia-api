package kr.proxia.client.oauth.github

data class GitHubUserInfo(
    val id: Long,
    val login: String,
    val email: String?,
    val name: String?,
    val avatarUrl: String?,
)
