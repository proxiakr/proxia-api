package kr.proxia.global.git.service

import kr.proxia.global.git.response.GitCommitResponse
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.io.File

@Service
class GitService(
    private val webClient: WebClient,
) {
    fun getLatestCommit(
        owner: String,
        repo: String,
        branch: String,
        token: String,
    ): GitCommitResponse {
        return try {
            webClient
                .get()
                .uri("https://api.github.com/repos/$owner/$repo/commits/$branch")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .bodyToMono(GitCommitResponse::class.java)
                .block()
                ?: throw IllegalStateException("GitHub returned empty response for $owner/$repo")
        } catch (e: WebClientResponseException) {
            when (e.statusCode.value()) {
                403 -> throw IllegalStateException("GitHub API rate limit exceeded", e)
                404 -> throw IllegalStateException("Repository or branch not found", e)
                401 -> throw IllegalStateException("GitHub token authentication failed", e)
                else -> throw IllegalStateException("Failed to fetch commit info", e)
            }
        }
    }

    fun cloneRepository(
        repositoryUrl: String,
        branch: String,
        targetDir: File,
        token: String? = null,
    ): Git {
        try {
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            targetDir.mkdirs()

            val command =
                Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(targetDir)
                    .setBranch(branch)
                    .setCloneAllBranches(false)
                    .setNoCheckout(false)
                    .setDepth(1)

            if (!token.isNullOrBlank()) {
                command.setCredentialsProvider(
                    UsernamePasswordCredentialsProvider("x-access-token", token)
                )
            }

            return command.call()
        } catch (e: GitAPIException) {
            throw IllegalStateException("Git clone failed for $repositoryUrl", e)
        } catch (e: Exception) {
            throw IllegalStateException("Unexpected error while cloning $repositoryUrl", e)
        }
    }
}
