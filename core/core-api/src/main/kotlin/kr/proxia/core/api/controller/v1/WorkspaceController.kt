package kr.proxia.core.api.controller.v1

import kr.proxia.core.api.controller.v1.request.CreateWorkspaceRequest
import kr.proxia.core.api.controller.v1.response.WorkspaceDetailResponse
import kr.proxia.core.api.controller.v1.response.WorkspaceMemberResponse
import kr.proxia.core.api.controller.v1.response.WorkspaceResponse
import kr.proxia.core.domain.WorkspaceService
import kr.proxia.storage.db.core.entity.User
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces")
class WorkspaceController(
    private val workspaceService: WorkspaceService,
) {
    @GetMapping
    fun getWorkspaces(
        @AuthenticationPrincipal userId: UUID,
    ): List<WorkspaceResponse> {
        val workspaces = workspaceService.getWorkspaces(userId)

        return workspaces.map {
            WorkspaceResponse(
                id = it.id,
                name = it.name,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }

    @GetMapping("/{workspaceId}")
    fun getWorkspace(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable workspaceId: UUID,
    ): WorkspaceDetailResponse {
        val workspace = workspaceService.getWorkspace(userId, workspaceId)
        val members = workspaceService.getWorkspaceMembers(userId, workspaceId)

        return WorkspaceDetailResponse(
            id = workspace.id,
            name = workspace.name,
            members =
                members.map {
                    WorkspaceMemberResponse(
                        user =
                            WorkspaceMemberResponse.User(
                                id = it.user.id,
                                email = it.user.email,
                            ),
                        role = it.role,
                    )
                },
            createdAt = workspace.createdAt,
            updatedAt = workspace.updatedAt,
        )
    }

    @PostMapping
    fun createWorkspace(
        @AuthenticationPrincipal userId: UUID,
        @RequestBody request: CreateWorkspaceRequest,
    ): WorkspaceResponse {
        val workspace = workspaceService.createWorkspace(userId, request.toDomain())

        return WorkspaceResponse(
            id = workspace.id,
            name = workspace.name,
            createdAt = workspace.createdAt,
            updatedAt = workspace.updatedAt,
        )
    }

    @DeleteMapping("/{workspaceId}")
    fun deleteWorkspace(@AuthenticationPrincipal userId: UUID, @PathVariable workspaceId: UUID) {
        workspaceService.deleteWorkspace(userId, workspaceId)
    }
}
