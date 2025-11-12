package kr.proxia.global.reverseproxy.service

interface ReverseProxyService {
    fun createServiceConfig(
        domain: String,
        containerName: String,
        internalPort: Int,
    )

    fun deleteServiceConfig(domain: String)

    fun isEnabled(): Boolean
}
