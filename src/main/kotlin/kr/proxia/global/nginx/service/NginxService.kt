package kr.proxia.global.nginx.service

import com.github.dockerjava.api.DockerClient
import kr.proxia.global.nginx.properties.NginxProperties
import kr.proxia.global.reverseproxy.service.ReverseProxyService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import java.io.File

@Service
@ConditionalOnProperty(prefix = "reverse-proxy", name = ["type"], havingValue = "nginx", matchIfMissing = true)
@EnableConfigurationProperties(NginxProperties::class)
class NginxService(
    private val nginxProperties: NginxProperties,
    private val dockerClient: DockerClient,
) : ReverseProxyService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isEnabled(): Boolean = nginxProperties.enabled

    override fun createServiceConfig(
        domain: String,
        containerName: String,
        internalPort: Int,
    ) {
        if (!nginxProperties.enabled) {
            logger.info("Nginx is disabled, skipping config generation")
            return
        }

        val configContent = generateNginxConfig(domain, containerName, internalPort)
        val configFile = File(nginxProperties.configDir, "$domain.conf")

        try {
            configFile.parentFile?.mkdirs()
            configFile.writeText(configContent)
            logger.info("Created nginx config for domain: $domain -> $containerName:$internalPort")

            reloadNginx()
        } catch (e: Exception) {
            logger.error("Failed to create nginx config for domain: $domain", e)
            throw e
        }
    }

    override fun deleteServiceConfig(domain: String) {
        if (!nginxProperties.enabled) {
            return
        }

        val configFile = File(nginxProperties.configDir, "$domain.conf")
        try {
            if (configFile.exists()) {
                configFile.delete()
                logger.info("Deleted nginx config for domain: $domain")
                reloadNginx()
            }
        } catch (e: Exception) {
            logger.error("Failed to delete nginx config for domain: $domain", e)
        }
    }

    private fun generateNginxConfig(
        domain: String,
        containerName: String,
        internalPort: Int,
    ): String {
        val sslConfig =
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

        val httpRedirect =
            if (nginxProperties.ssl.enabled) {
                """
                server {
                    listen 80;
                    server_name $domain;
                    return 301 https://${'$'}server_name${'$'}request_uri;
                }

                """.trimIndent()
            } else {
                ""
            }

        return """
            $httpRedirect
            server {
                $sslConfig
                server_name $domain;

                location / {
                    proxy_pass http://$containerName:$internalPort;
                    proxy_set_header Host ${'$'}host;
                    proxy_set_header X-Real-IP ${'$'}remote_addr;
                    proxy_set_header X-Forwarded-For ${'$'}proxy_add_x_forwarded_for;
                    proxy_set_header X-Forwarded-Proto ${'$'}scheme;

                    # WebSocket support
                    proxy_http_version 1.1;
                    proxy_set_header Upgrade ${'$'}http_upgrade;
                    proxy_set_header Connection "upgrade";

                    # Timeouts
                    proxy_connect_timeout 60s;
                    proxy_send_timeout 60s;
                    proxy_read_timeout 60s;
                }
            }
            """.trimIndent()
    }

    private fun reloadNginx() {
        try {
            val execCreateCmdResponse =
                dockerClient
                    .execCreateCmd(nginxProperties.containerName)
                    .withCmd("nginx", "-s", "reload")
                    .exec()

            dockerClient
                .execStartCmd(execCreateCmdResponse.id)
                .exec(object : com.github.dockerjava.api.async.ResultCallback.Adapter<com.github.dockerjava.api.model.Frame>() {})
                .awaitCompletion()

            logger.info("Nginx reloaded successfully")
        } catch (e: Exception) {
            logger.warn("Failed to reload nginx container, config will apply on next restart", e)
        }
    }
}
