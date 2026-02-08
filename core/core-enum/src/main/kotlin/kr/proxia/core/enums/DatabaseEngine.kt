package kr.proxia.core.enums

enum class DatabaseEngine(
    val port: Int,
) {
    MYSQL(3306),
    POSTGRESQL(5432),
    REDIS(6379),
}
