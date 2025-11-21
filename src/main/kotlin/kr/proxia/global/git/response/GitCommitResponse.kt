package kr.proxia.global.git.response


data class GitCommitResponse(
    val sha: String,
    val commit: CommitInfo,
) {
    data class CommitInfo(
        val message: String,
        val author: AuthorInfo,
    ) {
        data class AuthorInfo(
            val name: String,
        )
    }
}
