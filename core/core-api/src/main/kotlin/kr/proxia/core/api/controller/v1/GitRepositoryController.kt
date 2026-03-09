package kr.proxia.core.api.controller.v1

import kr.proxia.core.api.controller.v1.response.GitRepositoryResponse
import kr.proxia.core.domain.GitRepositoryService
import kr.proxia.core.support.OffsetLimit
import kr.proxia.core.support.response.PageResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/git/connections/{connectionId}/repositories")
class GitRepositoryController(
    private val gitRepositoryService: GitRepositoryService,
) {
    @GetMapping
    fun getRepositories(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
        @PathVariable connectionId: UUID,
        @RequestParam offset: Int,
        @RequestParam limit: Int,
    ): PageResponse<GitRepositoryResponse> {
        val repositories =
            gitRepositoryService.getRepositories(
                userId = userId,
                workspaceId = workspaceId,
                connectionId = connectionId,
                offsetLimit = OffsetLimit(offset, limit),
            )

        return PageResponse(
            content =
                repositories.content.map {
                    GitRepositoryResponse(
                        name = it.name,
                    )
                },
            hasNext = repositories.hasNext,
        )
    }
}
