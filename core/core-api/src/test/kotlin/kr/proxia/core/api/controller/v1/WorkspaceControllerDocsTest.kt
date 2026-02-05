package kr.proxia.core.api.controller.v1

import io.mockk.every
import io.mockk.mockk
import kr.proxia.core.api.controller.v1.request.CreateWorkspaceRequest
import kr.proxia.core.domain.CreateWorkspace
import kr.proxia.core.domain.WorkspaceService
import kr.proxia.core.enums.AuthProvider
import kr.proxia.core.enums.WorkspaceMemberRole
import kr.proxia.storage.db.core.entity.User
import kr.proxia.storage.db.core.entity.Workspace
import kr.proxia.storage.db.core.entity.WorkspaceMember
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
import java.util.UUID

@Tag("restdocs")
@ExtendWith(RestDocumentationExtension::class)
class WorkspaceControllerDocsTest {
    private lateinit var mockMvc: MockMvc
    private val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

    private val workspaceService = mockk<WorkspaceService>()
    private val testUserId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(WorkspaceController(workspaceService))
                .setCustomArgumentResolvers(TestAuthenticationPrincipalResolver(testUserId))
                .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
                .build()
    }

    @Test
    fun `워크스페이스 목록 조회`() {
        val workspace1 = Workspace(name = "My Workspace")
        val workspace2 = Workspace(name = "Team Project")

        every { workspaceService.getWorkspaces(testUserId) } returns listOf(workspace1, workspace2)

        mockMvc
            .perform(get("/api/v1/workspaces"))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "workspace-list",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("[].id").description("Workspace ID"),
                        fieldWithPath("[].name").description("Workspace name"),
                        fieldWithPath("[].createdAt").description("Creation timestamp"),
                        fieldWithPath("[].updatedAt").description("Last update timestamp"),
                    ),
                ),
            )
    }

    @Test
    fun `워크스페이스 상세 조회`() {
        val workspace = Workspace(name = "My Workspace")
        val user =
            User(
                email = "user@example.com",
                provider = AuthProvider.GOOGLE,
                providerId = "google-123",
            )
        val member =
            WorkspaceMember(
                workspace = workspace,
                user = user,
                role = WorkspaceMemberRole.OWNER,
            )

        every { workspaceService.getWorkspace(testUserId, workspace.id) } returns workspace
        every { workspaceService.getWorkspaceMembers(testUserId, workspace.id) } returns listOf(member)

        mockMvc
            .perform(get("/api/v1/workspaces/{workspaceId}", workspace.id))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "workspace-detail",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("Workspace ID"),
                        fieldWithPath("name").description("Workspace name"),
                        fieldWithPath("members").description("List of workspace members"),
                        fieldWithPath("members[].user.id").description("Member user ID"),
                        fieldWithPath("members[].user.email").description("Member email"),
                        fieldWithPath("members[].role").description("Member role (OWNER, ADMIN, MEMBER)"),
                        fieldWithPath("createdAt").description("Creation timestamp"),
                        fieldWithPath("updatedAt").description("Last update timestamp"),
                    ),
                ),
            )
    }

    @Test
    fun `워크스페이스 생성`() {
        val request = CreateWorkspaceRequest(name = "New Workspace")
        val workspace = Workspace(name = "New Workspace")

        every { workspaceService.createWorkspace(testUserId, CreateWorkspace(name = "New Workspace")) } returns workspace

        mockMvc
            .perform(
                post("/api/v1/workspaces")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "workspace-create",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("name").description("Workspace name"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("Created workspace ID"),
                        fieldWithPath("name").description("Workspace name"),
                        fieldWithPath("createdAt").description("Creation timestamp"),
                        fieldWithPath("updatedAt").description("Last update timestamp"),
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
