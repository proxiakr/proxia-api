package kr.proxia.global.reverseproxy.nginx.adapter

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.proxia.global.reverseproxy.ReverseProxyAdapter
import kr.proxia.global.reverseproxy.nginx.properties.NginxProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.File

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(prefix = "reverse-proxy", name = ["type"], havingValue = "nginx", matchIfMissing = true)
class NginxAdapter(
    private val nginxProperties: NginxProperties,
) : ReverseProxyAdapter {
    override val isEnabled: Boolean
        get() = nginxProperties.enabled

    override fun createMapping(
        domain: String,
        nodeEndpoint: String,
        hostPort: Int,
    ) {
        if (!nginxProperties.enabled) {
            logger.info { "Nginx disabled, skipping mapping setup" }
            return
        }

        val config = generateConfig(domain, nodeEndpoint, hostPort)
        val configFile = File("${nginxProperties.configDir}/$domain.conf")

        try {
            configFile.parentFile?.mkdirs()
            configFile.writeText(config)

            logger.info { "Created nginx mapping: $domain → http://$nodeEndpoint:$hostPort" }

            reloadNginx()
        } catch (e: Exception) {
            logger.error(e) { "Failed to create nginx config for $domain" }
            throw e
        }
    }

    override fun deleteMapping(domain: String) {
        val configFile = File("${nginxProperties.configDir}/$domain.conf")

        try {
            if (configFile.exists()) {
                configFile.delete()
                logger.info { "Deleted nginx config for $domain" }
                reloadNginx()
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete nginx config for $domain" }
        }
    }

    private fun generateConfig(
        domain: String,
        nodeEndpoint: String,
        hostPort: Int,
    ): String {
        val sslBlock =
            if (nginxProperties.ssl.enabled) {
                """
                listen 443 ssl http2;
                ssl_certificate ${nginxProperties.ssl.certDir}/$domain/fullchain.pem;
                ssl_certificate_key ${nginxProperties.ssl.certDir}/$domain/privkey.pem;
                ssl_protocols TLSv1.2 TLSv1.3;
                ssl_ciphers HIGH:!aNULL:!MD5;
                """.trimIndent()
            } else {
                "listen 80;"
            }

        val redirectHttp =
            if (nginxProperties.ssl.enabled) {
                """
                server {
                    listen 80;
                    server_name $domain;
                    return 301 https://$domain${'$'}request_uri;
                }
                """.trimIndent()
            } else {
                ""
            }

        return """
            $redirectHttp
            server {
                $sslBlock
                server_name $domain;

                location / {
                    proxy_pass http://$nodeEndpoint:$hostPort;

                    proxy_set_header Host ${'$'}host;
                    proxy_set_header X-Real-IP ${'$'}remote_addr;
                    proxy_set_header X-Forwarded-For ${'$'}proxy_add_x_forwarded_for;
                    proxy_set_header X-Forwarded-Proto ${'$'}scheme;

                    proxy_http_version 1.1;
                    proxy_set_header Upgrade ${'$'}http_upgrade;
                    proxy_set_header Connection "upgrade";
                }
            }
            """.trimIndent()
    }

    private fun reloadNginx() {
        try {
            val process =
                ProcessBuilder("nginx", "-s", "reload")
                    .redirectErrorStream(true)
                    .start()

            process.waitFor()

            logger.info { "Reloaded system nginx successfully" }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to reload nginx. Config will apply on next restart." }
        }
    }
}
