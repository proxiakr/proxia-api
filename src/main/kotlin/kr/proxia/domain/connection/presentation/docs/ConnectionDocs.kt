package kr.proxia.domain.connection.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.connection.presentation.request.CreateConnectionRequest
import kr.proxia.domain.connection.presentation.request.UpdateConnectionRequest
import kr.proxia.domain.connection.presentation.response.ConnectionResponse

@Tag(name = "Connection")
interface ConnectionDocs {
    @Operation(summary = "Create Connection")
    fun createConnection(
        projectId: Long,
        request: CreateConnectionRequest,
    )

    @Operation(summary = "Get Connections")
    fun getConnections(projectId: Long): List<ConnectionResponse>

    @Operation(summary = "Get Connection")
    fun getConnection(
        projectId: Long,
        connectionId: Long,
    ): ConnectionResponse

    @Operation(summary = "Update Connection")
    fun updateConnection(
        projectId: Long,
        connectionId: Long,
        request: UpdateConnectionRequest,
    )

    @Operation(summary = "Delete Connection")
    fun deleteConnection(
        projectId: Long,
        connectionId: Long,
    )
}
