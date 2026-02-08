package kr.proxia.storage.db.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.proxia.core.enums.GitProvider
import kr.proxia.storage.db.core.converter.EncryptConverter
import java.time.LocalDateTime

@Entity
@Table(name = "git_connections")
class GitConnection(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    val workspace: Workspace,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: GitProvider,
    @Column(nullable = false)
    val installationId: String,
    @Convert(converter = EncryptConverter::class)
    var accessToken: String? = null,
    var expiresAt: LocalDateTime? = null,
) : BaseEntity()
