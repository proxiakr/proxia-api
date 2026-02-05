package kr.proxia.storage.db.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "workspaces")
class Workspace(
    @Column(nullable = false)
    val name: String,
) : BaseEntity()
