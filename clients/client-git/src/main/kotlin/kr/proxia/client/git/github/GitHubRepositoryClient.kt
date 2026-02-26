package kr.proxia.client.git.github

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class GitHubRepositoryClient(
    private val githubRestClient: RestClient,
) {
    fun getGithubRepositories(accessToken: String): List<GitHubRepository> =
        githubRestClient.get()
            .uri("https://api.github.com/installation/repositories")
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body<GitHubRepositoriesResponse>()!!.repositories

    fun verifyRepositoryAccess(token: String, repoFullName: String): Boolean =
        try {
            githubRestClient.get()
                .uri("https://api.github.com/repos/$repoFullName")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .toBodilessEntity()
                .statusCode.is2xxSuccessful
        } catch (_: Exception) {
            false
        }
}
