package kr.proxia.domain.connection.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.connection.presentation.request.CreateConnectionRequest
import kr.proxia.domain.connection.presentation.request.UpdateConnectionRequest
import kr.proxia.domain.connection.presentation.response.ConnectionResponse
import java.util.UUID

@Tag(name = "Connection")
interface ConnectionDocs {
    @Operation(summary = "Create Connection")
    fun createConnection(
        projectId: UUID,
        request: CreateConnectionRequest,
    )

    @Operation(summary = "Get Connections")
    fun getConnections(projectId: UUID): List<ConnectionResponse>

    @Operation(summary = "Get Connection")
    fun getConnection(
        projectId: UUID,
        connectionId: UUID,
    ): ConnectionResponse

    @Operation(summary = "Update Connection")
    fun updateConnection(
        projectId: UUID,
        connectionId: UUID,
        request: UpdateConnectionRequest,
    )

    @Operation(summary = "Delete Connection")
    fun deleteConnection(
        projectId: UUID,
        connectionId: UUID,
    )
}
