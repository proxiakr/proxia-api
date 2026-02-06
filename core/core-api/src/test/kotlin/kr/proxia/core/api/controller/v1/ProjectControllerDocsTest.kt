package kr.proxia.core.api.controller.v1

import io.mockk.every
import io.mockk.mockk
import kr.proxia.core.api.controller.v1.request.CreateProjectRequest
import kr.proxia.core.domain.CreateProject
import kr.proxia.core.domain.ProjectService
import kr.proxia.storage.db.core.entity.Project
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
import java.util.UUID

@Tag("restdocs")
@ExtendWith(RestDocumentationExtension::class)
class ProjectControllerDocsTest {
    private lateinit var mockMvc: MockMvc
    private val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

    private val projectService = mockk<ProjectService>()
    private val testUserId: UUID = UUID.randomUUID()
    private val testWorkspaceId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(ProjectController(projectService))
                .setCustomArgumentResolvers(TestAuthenticationPrincipalResolver(testUserId))
                .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
                .build()
    }

    @Test
    fun `프로젝트 목록 조회`() {
        val workspace = Workspace(name = "Test Workspace")
        val project1 = Project(name = "Project Alpha", subdomain = "alpha", workspace = workspace)
        val project2 = Project(name = "Project Beta", subdomain = "beta", workspace = workspace)

        every { projectService.getProjects(testUserId, testWorkspaceId) } returns listOf(project1, project2)

        mockMvc
            .perform(get("/api/v1/workspaces/{workspaceId}/projects", testWorkspaceId))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "project-list",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                    ),
                    responseFields(
                        fieldWithPath("[].id").description("Project ID"),
                        fieldWithPath("[].name").description("Project name"),
                        fieldWithPath("[].createdAt").description("Creation timestamp"),
                        fieldWithPath("[].updatedAt").description("Last update timestamp"),
                    ),
                ),
            )
    }

    @Test
    fun `프로젝트 상세 조회`() {
        val workspace = Workspace(name = "Test Workspace")
        val project = Project(name = "Project Alpha", subdomain = "alpha", workspace = workspace)

        every { projectService.getProject(testUserId, testWorkspaceId, project.id) } returns project

        mockMvc
            .perform(get("/api/v1/workspaces/{workspaceId}/projects/{projectId}", testWorkspaceId, project.id))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "project-detail",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                        parameterWithName("projectId").description("Project ID"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("Project ID"),
                        fieldWithPath("name").description("Project name"),
                        fieldWithPath("subdomain").description("Project subdomain"),
                        fieldWithPath("createdAt").description("Creation timestamp"),
                        fieldWithPath("updatedAt").description("Last update timestamp"),
                    ),
                ),
            )
    }

    @Test
    fun `프로젝트 생성`() {
        val request = CreateProjectRequest(name = "New Project", subdomain = "new-project")
        val workspace = Workspace(name = "Test Workspace")
        val project = Project(name = "New Project", subdomain = "new-project", workspace = workspace)

        every {
            projectService.createProject(testUserId, testWorkspaceId, CreateProject(name = "New Project", subdomain = "new-project"))
        } returns project

        mockMvc
            .perform(
                post("/api/v1/workspaces/{workspaceId}/projects", testWorkspaceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "project-create",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                    ),
                    requestFields(
                        fieldWithPath("name").description("Project name"),
                        fieldWithPath("subdomain").description("Project subdomain (lowercase alphanumeric with hyphens)"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("Created project ID"),
                        fieldWithPath("name").description("Project name"),
                        fieldWithPath("createdAt").description("Creation timestamp"),
                        fieldWithPath("updatedAt").description("Last update timestamp"),
                    ),
                ),
            )
    }

    @Test
    fun `프로젝트 삭제`() {
        val projectId = UUID.randomUUID()

        every { projectService.deleteProject(testUserId, testWorkspaceId, projectId) } returns Unit

        mockMvc
            .perform(delete("/api/v1/workspaces/{workspaceId}/projects/{projectId}", testWorkspaceId, projectId))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "project-delete",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                        parameterWithName("projectId").description("Project ID to delete"),
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
