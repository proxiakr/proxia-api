package kr.proxia.domain.git.infra.client

import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.infra.data.GitRepository
import kr.proxia.domain.git.infra.data.GitRepositoryDetail
import kr.proxia.global.error.BusinessException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class GithubRepositoryClient(
    private val webClient: WebClient,
) : GitRepositoryClient {
    override fun getGitRepository(
        accessToken: String,
        url: String,
        branch: String
    ): GitRepositoryDetail = try {
        val pathSegments = url.replace("https://github.com/", "").split("/")
        val owner = pathSegments[0]
        val repository = pathSegments[1].replace(".git", "")

        val branchResponse = webClient.get()
            .uri("https://api.github.com/repos/$owner/$repository/branches/$branch")
            .headers { headers ->
                headers.setBearerAuth(accessToken)
                headers.set("Accept", "application/vnd.github.v3+json")
            }
            .retrieve()
            .bodyToMono<GithubBranchResponse>()
            .block() ?: throw BusinessException(GitError.GithubApiError)

        val repositoryResponse = webClient.get()
            .uri("https://api.github.com/repos/$owner/$repository")
            .headers { headers ->
                headers.setBearerAuth(accessToken)
            }
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .block() ?: throw BusinessException(GitError.GithubApiError)

        val repositoryId = repositoryResponse["id"] as Long
        val fullName = repositoryResponse["full_name"] as String

        GitRepositoryDetail(
            id = repositoryId,
            fullName = fullName,
            branch = branchResponse.name,
            commitSha = branchResponse.commit.sha,
            commitMessage = branchResponse.commit.commit.message,
            commitAuthor = branchResponse.commit.commit.author.name
        )
    } catch (e: WebClientResponseException) {
        when (e.statusCode.value()) {
            404 -> throw BusinessException(GitError.NotFound)
            401 -> throw BusinessException(GitError.AccessDenied)
            else -> throw BusinessException(GitError.GithubApiError)
        }
    } catch (_: Exception) {
        throw BusinessException(GitError.GithubApiError)
    }

    override fun getGitRepositories(accessToken: String): List<GitRepository> =
        try {
            webClient
                .get()
                .uri("https://api.github.com/user/repos")
                .headers { headers ->
                    headers.setBearerAuth(accessToken)
                    headers.set("Accept", "application/vnd.github.v3+json")
                }.retrieve()
                .bodyToFlux<GitRepository>()
                .collectList()
                .block() ?: throw BusinessException(GitError.GithubApiError)
        } catch (e: WebClientResponseException) {
            when (e.statusCode.value()) {
                401 -> throw BusinessException(GitError.AccessDenied)
                else -> throw BusinessException(GitError.GithubApiError)
            }
        } catch (_: Exception) {
            throw BusinessException(GitError.GithubApiError)
        }

    private data class GithubBranchResponse(
        val name: String,
        val commit: CommitNode
    )

    private data class CommitNode(
        val sha: String,
        val commit: CommitDetail
    )

    private data class CommitDetail(
        val author: AuthorInfo,
        val message: String
    )

    private data class AuthorInfo(
        val name: String
    )
}
