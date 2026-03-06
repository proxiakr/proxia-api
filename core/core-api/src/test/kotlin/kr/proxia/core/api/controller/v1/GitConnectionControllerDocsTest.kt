package kr.proxia.core.api.controller.v1

import io.mockk.every
import io.mockk.mockk
import kr.proxia.core.api.controller.v1.request.CreateGitConnectionRequest
import kr.proxia.core.domain.CreateGitConnection
import kr.proxia.core.domain.GitConnectionService
import kr.proxia.core.enums.GitProvider
import kr.proxia.storage.db.core.entity.GitConnection
import kr.proxia.storage.db.core.entity.Workspace
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.time.LocalDateTime
import java.util.UUID

@Tag("restdocs")
@ExtendWith(RestDocumentationExtension::class)
class GitConnectionControllerDocsTest {
    private lateinit var mockMvc: MockMvc
    private val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

    private val gitConnectionService = mockk<GitConnectionService>()
    private val testUserId: UUID = UUID.randomUUID()
    private val testWorkspaceId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(GitConnectionController(gitConnectionService))
                .setCustomArgumentResolvers(TestAuthenticationPrincipalResolver(testUserId))
                .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
                .build()
    }

    @Test
    fun `Git 연결 목록 조회`() {
        val workspace = Workspace(name = "Test Workspace")

        val connection1 =
            GitConnection(
                workspace = workspace,
                name = "Test Connection 1",
                provider = GitProvider.GITHUB,
                installationId = "test-installation-id-1",
                accessToken = "test-access-token-1",
                expiresAt = LocalDateTime.now().plusSeconds(60L),
            )

        val connection2 =
            GitConnection(
                workspace = workspace,
                name = "Test Connection 2",
                provider = GitProvider.GITHUB,
                installationId = "test-installation-id-2",
                accessToken = "test-access-token-2",
                expiresAt = LocalDateTime.now().plusSeconds(120L),
            )

        every { gitConnectionService.getConnections(testUserId, testWorkspaceId) } returns
            listOf(
                connection1,
                connection2,
            )

        mockMvc
            .perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/git/connections",
                    testWorkspaceId,
                ),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "git-connection-list",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                    ),
                    responseFields(
                        fieldWithPath("[].id").description("Git Connection ID"),
                        fieldWithPath("[].name").description("Git Connection Name"),
                        fieldWithPath("[].provider").description("Git Connection Provider"),
                    ),
                ),
            )
    }

    @Test
    fun `Git 연결 생성`() {
        val request =
            CreateGitConnectionRequest(
                installationId = "test-installation-id",
                name = "Test Connection",
                provider = GitProvider.GITHUB,
            )

        every {
            gitConnectionService.createConnection(
                testUserId,
                testWorkspaceId,
                CreateGitConnection(
                    installationId = "test-installation-id",
                    name = "Test Connection",
                    provider = GitProvider.GITHUB,
                ),
            )
        } returns Unit

        mockMvc
            .perform(
                post("/api/v1/workspaces/{workspaceId}/git/connections", testWorkspaceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "git-connection-create",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                    ),
                    requestFields(
                        fieldWithPath("installationId").description("Installation ID"),
                        fieldWithPath("name").description("Git Connection Name"),
                        fieldWithPath("provider").description("Git Connection Provider"),
                    ),
                ),
            )
    }

    @Test
    fun `Git 연결 삭제`() {
        val connectionId = UUID.randomUUID()

        every { gitConnectionService.deleteConnection(testUserId, testWorkspaceId, connectionId) } returns Unit

        mockMvc
            .perform(
                delete(
                    "/api/v1/workspaces/{workspaceId}/git/connections/{connectionId}",
                    testWorkspaceId,
                    connectionId,
                ),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "git-connection-delete",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                        parameterWithName("connectionId").description("Git Connection ID"),
                    ),
                ),
            )
    }

    private class TestAuthenticationPrincipalResolver(
        private val userId: UUID,
    ) : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean =
            parameter.hasParameterAnnotation(AuthenticationPrincipal::class.java)

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?,
        ): Any = userId
    }
}
