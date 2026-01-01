package kr.proxia.domain.git.infra.data

data class GitRepositoryDetail(
    val id: Long,
    val fullName: String,
    val branch: String,
    val commitSha: String,
    val commitMessage: String,
    val commitAuthor: String,
)
