package kr.proxia.core.domain

import kr.proxia.core.enums.DatabaseEngine

data class CreateDatabaseService(
    val name: String,
    val x: Double,
    val y: Double,
    val engine: DatabaseEngine,
)
