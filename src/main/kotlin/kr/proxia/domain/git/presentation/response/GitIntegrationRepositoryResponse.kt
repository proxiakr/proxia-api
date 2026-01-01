package kr.proxia.domain.git.presentation.response

import kr.proxia.domain.git.infra.data.GitRepository

data class GitIntegrationRepositoryResponse(
    val name: String,
) {
    companion object {
        fun of(repository: GitRepository) =
            GitIntegrationRepositoryResponse(
                name = repository.name,
            )

        fun of(repositories: List<GitRepository>) = repositories.map { of(it) }
    }
}
