package kr.proxia.core.domain

import kr.proxia.core.enums.DatabaseEngine
import java.util.UUID

object DatabaseCredentialGenerator {
    fun generatePassword(): String = UUID.randomUUID().toString().replace("-", "")

    fun generateUsername(engine: DatabaseEngine): String =
        when (engine) {
            DatabaseEngine.POSTGRESQL -> "postgres"
            else -> "admin"
        }
}
