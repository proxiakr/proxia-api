package kr.proxia.core.domain

import kr.proxia.core.enums.DatabaseEngine

enum class DatabaseEngineConfig(
    val image: String,
    val dataPath: String,
    val supportedVersions: List<String>,
) {
    MYSQL("mysql", "/var/lib/mysql", listOf("8.0", "8.4", "9.0", "9.1")),
    POSTGRESQL("postgres", "/var/lib/postgresql/data", listOf("14", "15", "16", "17")),
    REDIS("redis", "/data", listOf("7.0", "7.2", "7.4", "8.0")),
    ;

    companion object {
        fun of(engine: DatabaseEngine) = valueOf(engine.name)
    }

    fun env(
        database: String,
        username: String,
        password: String,
    ): List<String> =
        when (this) {
            MYSQL ->
                listOf(
                    "MYSQL_DATABASE=$database",
                    "MYSQL_USER=$username",
                    "MYSQL_PASSWORD=$password",
                    "MYSQL_ROOT_PASSWORD=$password",
                )
            POSTGRESQL ->
                listOf(
                    "POSTGRES_DB=$database",
                    "POSTGRES_USER=$username",
                    "POSTGRES_PASSWORD=$password",
                )
            REDIS -> emptyList()
        }

    fun commandArgs(password: String): List<String> =
        when (this) {
            REDIS ->
                if (password.isNotBlank()) {
                    listOf("redis-server", "--requirepass", password)
                } else {
                    emptyList()
                }
            else -> emptyList()
        }
}
