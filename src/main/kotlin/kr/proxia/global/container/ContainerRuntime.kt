package kr.proxia.global.container

interface ContainerRuntime {
    fun exec(
        containerName: String,
        command: List<String>,
    )
}
