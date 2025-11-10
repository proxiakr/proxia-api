package kr.proxia.domain.resource.presentation.response

import kr.proxia.domain.resource.domain.entity.DatabaseResourceEntity
import kr.proxia.domain.service.domain.enums.DatabaseType

data class DatabaseResourceResponse(
    val type: DatabaseType,
    val database: String?,
    val username: String?,
) {
    companion object {
        fun of(databaseResource: DatabaseResourceEntity) =
            DatabaseResourceResponse(
                type = databaseResource.type,
                database = databaseResource.database,
                username = databaseResource.username,
            )
    }
}
