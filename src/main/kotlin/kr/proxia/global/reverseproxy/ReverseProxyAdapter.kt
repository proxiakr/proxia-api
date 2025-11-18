package kr.proxia.global.reverseproxy

interface ReverseProxyAdapter {
    val isEnabled: Boolean

    fun createMapping(
        domain: String,
        nodeEndpoint: String,
        hostPort: Int,
    )

    fun deleteMapping(domain: String)
}
