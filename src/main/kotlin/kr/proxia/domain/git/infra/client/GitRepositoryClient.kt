package kr.proxia.domain.git.infra.client

import kr.proxia.domain.git.infra.data.GitRepository
import kr.proxia.domain.git.infra.data.GitRepositoryDetail

interface GitRepositoryClient {
    fun getGitRepository(
        accessToken: String,
        url: String,
        branch: String,
    ): GitRepositoryDetail

    fun getGitRepositories(accessToken: String): List<GitRepository>
}
