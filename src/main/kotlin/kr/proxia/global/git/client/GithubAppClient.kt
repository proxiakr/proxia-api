package kr.proxia.global.git.client

import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class GithubAppClient(
    private val webClient: WebClient,
) {
    fun getLatestCommit(
        owner: String,
        repo: String,
        branch: String,
        token: String,
    ): GitCommitResponse {
        return webClient
            .get()
            .uri("https://api.github.com/repos/$owner/$repo/commits/$branch")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .retrieve()
            .bodyToMono(GitCommitResponse::class.java)
            .block()!!
    }

    data class GitCommitResponse(
        val sha: String,
        val commit: CommitInfo
    )

    data class CommitInfo(
        val message: String,
        val author: AuthorInfo
    )

    data class AuthorInfo(
        val name: String
    )
}
