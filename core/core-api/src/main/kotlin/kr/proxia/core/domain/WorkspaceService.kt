package kr.proxia.core.domain

import kr.proxia.core.enums.WorkspaceMemberRole
import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.storage.db.core.entity.User
import kr.proxia.storage.db.core.entity.Workspace
import kr.proxia.storage.db.core.entity.WorkspaceMember
import kr.proxia.storage.db.core.repository.UserRepository
import kr.proxia.storage.db.core.repository.WorkspaceMemberRepository
import kr.proxia.storage.db.core.repository.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WorkspaceService(
    private val workspaceRepository: WorkspaceRepository,
    private val userRepository: UserRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
) {
    fun getWorkspaces(userId: UUID): List<Workspace> = workspaceRepository.findByMember(userId)

    fun getWorkspace(
        userId: UUID,
        workspaceId: UUID,
    ): Workspace {
        val workspace =
            workspaceRepository.findByIdAndMember(workspaceId, userId)
                ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        return workspace
    }

    fun createWorkspace(
        userId: UUID,
        createWorkspace: CreateWorkspace,
    ): Workspace {
        val workspace = workspaceRepository.save(Workspace(name = createWorkspace.name))
        val user = userRepository.findByIdOrNull(userId) ?: throw CoreException(ErrorType.USER_NOT_FOUND)
        val member =
            WorkspaceMember(
                user = user,
                workspace = workspace,
                role = WorkspaceMemberRole.OWNER,
            )

        workspaceMemberRepository.save(member)

        return workspace
    }

    fun createDefaultWorkspace(user: User): Workspace {
        val name = "${user.name}'s Projects"
        val workspace = workspaceRepository.save(Workspace(name = name))
        val member =
            WorkspaceMember(
                user = user,
                workspace = workspace,
                role = WorkspaceMemberRole.OWNER,
            )

        workspaceMemberRepository.save(member)

        return workspace
    }

    fun getWorkspaceMembers(
        userId: UUID,
        workspaceId: UUID,
    ): List<WorkspaceMember> {
        val workspace = getWorkspace(userId, workspaceId)

        return workspaceMemberRepository.findByWorkspaceWithUser(workspace)
    }

    fun deleteWorkspace(
        userId: UUID,
        workspaceId: UUID,
    ) {
        val workspace = getWorkspace(userId, workspaceId)

        workspaceRepository.delete(workspace)
    }
}
