package kr.proxia.client.git.github

import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GitHubRepositoryClient(
    private val githubWebRestClient: RestClient,
) {
    fun getGithubRepositories(
        accessToken: String,
        page: Int,
        perPage: Int,
    ): GitHubRepositoriesResponse {
        val entity =
            githubWebRestClient
                .get()
                .uri {
                    it
                        .path("/installation/repositories")
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .build()
                }.header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .toEntity(GitHubRepositoriesApiResponse::class.java)

        return GitHubRepositoriesResponse(
            repositories = entity.body!!.repositories,
            hasNext = entity.headers.hasNextPage(),
        )
    }

    fun verifyRepositoryAccess(
        token: String,
        repoFullName: String,
    ): Boolean =
        try {
            githubWebRestClient
                .get()
                .uri("/repos/$repoFullName")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .toBodilessEntity()
                .statusCode.is2xxSuccessful
        } catch (_: Exception) {
            false
        }

    private fun HttpHeaders.hasNextPage(): Boolean {
        val link = getFirst("Link") ?: return false
        return NEXT_PAGE_REGEX.containsMatchIn(link)
    }

    companion object {
        private val NEXT_PAGE_REGEX = Regex("""<([^>]+)>\s*;\s*rel="next"""")
    }
}
