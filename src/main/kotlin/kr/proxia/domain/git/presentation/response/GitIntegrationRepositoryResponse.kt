package kr.proxia.domain.git.presentation.response

import kr.proxia.domain.git.infra.data.GithubRepository

data class GitIntegrationRepositoryResponse(
    val name: String,
) {
    companion object {
        fun of(repository: GithubRepository) =
            GitIntegrationRepositoryResponse(
                name = repository.name,
            )

        fun of(repositories: List<GithubRepository>) = repositories.map { of(it) }
    }
}
