package kr.proxia.client.git.github

import kr.proxia.client.git.GitProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class GitHubConnectionClient(
    private val githubRestClient: RestClient,
    private val jwtProvider: GitHubAppJwtProvider,
    private val properties: GitProperties,
) {
    fun getInstallationToken(installationId: String): InstallationTokenResponse {
        val jwt = jwtProvider.createAsyncJwt(properties.github.appId, properties.github.privateKey)

        return githubRestClient.post()
            .uri("https://api.github.com/app/installations/$installationId/access_tokens")
            .header("Authorization", "Bearer $jwt")
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body<InstallationTokenResponse>()!!
    }
}
