package kr.proxia.domain.connection.presentation.controller

import kr.proxia.domain.connection.application.service.ConnectionService
import kr.proxia.domain.connection.presentation.docs.ConnectionDocs
import kr.proxia.domain.connection.presentation.request.CreateConnectionRequest
import kr.proxia.domain.connection.presentation.request.UpdateConnectionRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/projects/{projectId}/connections")
class ConnectionController(
    private val connectionService: ConnectionService,
) : ConnectionDocs {
    @PostMapping
    override fun createConnection(
        @PathVariable projectId: Long,
        @RequestBody request: CreateConnectionRequest,
    ) = connectionService.createConnection(projectId, request)

    @GetMapping
    override fun getConnections(
        @PathVariable projectId: Long,
    ) = connectionService.getConnections(projectId)

    @GetMapping("/{connectionId}")
    override fun getConnection(
        @PathVariable projectId: Long,
        @PathVariable connectionId: Long,
    ) = connectionService.getConnection(connectionId)

    @PutMapping("/{connectionId}")
    override fun updateConnection(
        @PathVariable projectId: Long,
        @PathVariable connectionId: Long,
        @RequestBody request: UpdateConnectionRequest,
    ) = connectionService.updateConnection(connectionId, request)

    @DeleteMapping("/{connectionId}")
    override fun deleteConnection(
        @PathVariable projectId: Long,
        @PathVariable connectionId: Long,
    ) = connectionService.deleteConnection(connectionId)
}
