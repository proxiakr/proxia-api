package kr.proxia.domain.webhook.presentation.request

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubWebhookPayload(
    val ref: String,
    val repository: Repository,
    @field:JsonProperty("head_commit")
    val headCommit: Commit?,
) {
    data class Repository(
        @field:JsonProperty("clone_url")
        val cloneUrl: String,
    )

    data class Commit(
        val id: String,
        val message: String,
        val author: Author,
    ) {
        data class Author(
            val name: String,
        )
    }

    val branch: String
        get() = ref.removePrefix("refs/heads/")

    val repositoryUrl: String
        get() = repository.cloneUrl
}
