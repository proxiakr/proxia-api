package kr.proxia.core.api.controller.v1

import kr.proxia.core.api.controller.v1.request.CreateGitConnectionRequest
import kr.proxia.core.api.controller.v1.response.GitConnectionResponse
import kr.proxia.core.domain.GitConnectionService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/git/connections")
class GitConnectionController(
    private val gitConnectionService: GitConnectionService,
) {
    @GetMapping
    fun getConnections(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
    ): List<GitConnectionResponse> {
        val connections = gitConnectionService.getConnections(
            userId = userId,
            workspaceId = workspaceId,
        )

        return connections.map {
            GitConnectionResponse(
                id = it.id,
                name = it.name,
                provider = it.provider,
            )
        }
    }

    @PostMapping
    fun createConnection(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
        @RequestBody request: CreateGitConnectionRequest,
    ) {
        gitConnectionService.createConnection(
            userId = userId,
            workspaceId = workspaceId,
            createGitConnection = request.toDomain(),
        )
    }

    @PostMapping("/{connectionId}")
    fun deleteConnection(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
        @PathVariable connectionId: UUID,
    ) {
        gitConnectionService.deleteConnection(
            userId = userId,
            workspaceId = workspaceId,
            connectionId = connectionId,
        )
    }
}
