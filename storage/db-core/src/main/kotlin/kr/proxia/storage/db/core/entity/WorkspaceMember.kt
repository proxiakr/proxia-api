package kr.proxia.storage.db.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.proxia.core.enums.WorkspaceMemberRole

@Entity
@Table(name = "workspace_members")
class WorkspaceMember(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    val workspace: Workspace,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: WorkspaceMemberRole,
) : BaseEntity()
