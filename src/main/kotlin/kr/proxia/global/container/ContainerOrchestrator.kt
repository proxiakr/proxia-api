package kr.proxia.global.container

interface ContainerOrchestrator {
    fun createContainer(
        endpoint: String,
        spec: ContainerSpec,
    ): String

    fun startContainer(
        endpoint: String,
        containerId: String,
    )

    fun stopContainer(
        endpoint: String,
        containerId: String,
    )

    fun deleteContainer(
        endpoint: String,
        containerId: String,
    )

    fun getLogs(
        endpoint: String,
        containerId: String,
        tail: Int = 100,
    ): String

    fun isRunning(
        endpoint: String,
        containerId: String,
    ): Boolean

    fun getAssignedPort(
        endpoint: String,
        containerId: String,
        internalPort: Int,
    ): Int?
}
