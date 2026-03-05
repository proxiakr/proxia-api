package kr.proxia.core.api.controller.v1

import io.mockk.every
import io.mockk.mockk
import kr.proxia.core.api.controller.v1.request.CreateAppServiceRequest
import kr.proxia.core.api.controller.v1.request.CreateDatabaseServiceRequest
import kr.proxia.core.domain.CreateAppService
import kr.proxia.core.domain.CreateDatabaseService
import kr.proxia.core.domain.ServiceService
import kr.proxia.core.enums.DatabaseEngine
import kr.proxia.core.enums.Framework
import kr.proxia.core.enums.GitProvider
import kr.proxia.core.enums.ServiceStatus
import kr.proxia.storage.db.core.entity.AppService
import kr.proxia.storage.db.core.entity.DatabaseService
import kr.proxia.storage.db.core.entity.GitConnection
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
class ServiceControllerDocsTest {
    private lateinit var mockMvc: MockMvc
    private val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

    private val serviceService = mockk<ServiceService>()
    private val testUserId: UUID = UUID.randomUUID()
    private val testWorkspaceId = UUID.randomUUID()
    private val testProjectId = UUID.randomUUID()
    private val testGitConnectionId = UUID.randomUUID()

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(ServiceController(serviceService))
                .setCustomArgumentResolvers(TestAuthenticationPrincipalResolver(testUserId))
                .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
                .build()
    }

    @Test
    fun `서비스 목록 조회`() {
        val workspace = Workspace(name = "Test Workspace")
        val project = Project(name = "Project Alpha", subdomain = "alpha", workspace = workspace)

        val gitConnection =
            GitConnection(
                workspace = workspace,
                name = "Test Connection",
                provider = GitProvider.GITHUB,
                installationId = "test_installation_id",
            )

        val appService =
            AppService(
                name = "Test App",
                x = 0.1,
                y = 0.1,
                project = project,
                status = ServiceStatus.RUNNING,
                repoFullName = "test_repo_full_name",
                branch = "main",
                port = 1234,
                framework = Framework.SPRING_BOOT,
                rootDirectory = ".",
                gitConnection = gitConnection,
            )

        val databaseService =
            DatabaseService(
                name = "Test Database",
                x = 10.0,
                y = 10.0,
                project = project,
                status = ServiceStatus.RUNNING,
                engine = DatabaseEngine.MYSQL,
                version = "8.0.39",
                database = "test_database",
                username = "test_username",
                password = "test_password",
            )

        every { serviceService.getServices(testUserId, testWorkspaceId, testProjectId) } returns
            listOf(
                appService,
                databaseService,
            )

        mockMvc
            .perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/projects/{projectId}/services",
                    testWorkspaceId,
                    testProjectId,
                ),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "service-list",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                        parameterWithName("projectId").description("Project ID"),
                    ),
                    responseFields(
                        fieldWithPath("[].id").description("Service ID"),
                        fieldWithPath("[].name").description("Service name"),
                        fieldWithPath("[].x").description("X coordinate of the service"),
                        fieldWithPath("[].y").description("Y coordinate of the service"),
                        fieldWithPath("[].status").description("Service status"),
                        fieldWithPath("[].createdAt").description("Creation timestamp"),
                        fieldWithPath("[].updatedAt").description("Last update timestamp"),
                    ),
                ),
            )
    }

    @Test
    fun `서비스 상세 조회`() {
        val workspace = Workspace(name = "Test Workspace")
        val project = Project(name = "Project Alpha", subdomain = "alpha", workspace = workspace)

        val gitConnection =
            GitConnection(
                workspace = workspace,
                name = "Test Connection",
                provider = GitProvider.GITHUB,
                installationId = "test_installation_id",
            )

        val appService =
            AppService(
                name = "Test App",
                x = 0.1,
                y = 0.1,
                project = project,
                status = ServiceStatus.RUNNING,
                repoFullName = "test_repo_full_name",
                branch = "main",
                port = 1234,
                framework = Framework.SPRING_BOOT,
                rootDirectory = ".",
                gitConnection = gitConnection,
            )

        every {
            serviceService.getService(
                testUserId,
                testWorkspaceId,
                testProjectId,
                appService.id,
            )
        } returns appService

        mockMvc
            .perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/projects/{projectId}/services/{serviceId}",
                    testWorkspaceId,
                    testProjectId,
                    appService.id,
                ),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "service-detail",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                        parameterWithName("projectId").description("Project ID"),
                        parameterWithName("serviceId").description("Service ID"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("Service ID"),
                        fieldWithPath("name").description("Service name"),
                        fieldWithPath("x").description("X coordinate of the service"),
                        fieldWithPath("y").description("Y coordinate of the service"),
                        fieldWithPath("status").description("Service status"),
                        fieldWithPath("createdAt").description("Creation timestamp"),
                        fieldWithPath("updatedAt").description("Last update timestamp"),
                    ),
                ),
            )
    }

    @Test
    fun `앱 서비스 생성`() {
        val workspace = Workspace(name = "Test Workspace")
        val project = Project(name = "Project Alpha", subdomain = "alpha", workspace = workspace)

        val gitConnection =
            GitConnection(
                workspace = workspace,
                name = "Test Connection",
                provider = GitProvider.GITHUB,
                installationId = "test_installation_id",
            )

        val request =
            CreateAppServiceRequest(
                name = "Test App",
                x = 0.1,
                y = 0.1,
                repoFullName = "test_repo_full_name",
                branch = "main",
                port = 1234,
                framework = Framework.SPRING_BOOT,
                buildCommand = "build_command",
                startCommand = "start_command",
                gitConnectionId = gitConnection.id,
            )

        val appService =
            AppService(
                name = "Test App",
                x = 0.1,
                y = 0.1,
                project = project,
                status = ServiceStatus.RUNNING,
                repoFullName = "test_repo_full_name",
                branch = "main",
                port = 1234,
                framework = Framework.SPRING_BOOT,
                rootDirectory = ".",
                gitConnection = gitConnection,
            )

        every {
            serviceService.createAppService(
                testUserId,
                testWorkspaceId,
                testProjectId,
                CreateAppService(
                    name = "Test App",
                    x = 0.1,
                    y = 0.1,
                    repoFullName = "test_repo_full_name",
                    branch = "main",
                    port = 1234,
                    framework = Framework.SPRING_BOOT,
                    buildCommand = "build_command",
                    startCommand = "start_command",
                    gitConnectionId = gitConnection.id,
                ),
            )
        } returns appService

        mockMvc
            .perform(
                post(
                    "/api/v1/workspaces/{workspaceId}/projects/{projectId}/services/app",
                    testWorkspaceId,
                    testProjectId,
                ).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "service-create-app",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                        parameterWithName("projectId").description("Project ID"),
                    ),
                    requestFields(
                        fieldWithPath("name").description("Service name"),
                        fieldWithPath("x").description("X coordinate of the service"),
                        fieldWithPath("y").description("Y coordinate of the service"),
                        fieldWithPath("repoFullName").description("Full name of the repository containing the service"),
                        fieldWithPath("branch").description("Branch of the repository containing the service"),
                        fieldWithPath("port").description("Port of the service"),
                        fieldWithPath("framework").description("Framework of the service"),
                        fieldWithPath("buildCommand").description("Build command of the service"),
                        fieldWithPath("startCommand").description("Start command of the service"),
                        fieldWithPath("gitConnectionId").description("ID of git connection containing the repository"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("Service ID"),
                        fieldWithPath("name").description("Service name"),
                        fieldWithPath("x").description("X coordinate of the service"),
                        fieldWithPath("y").description("Y coordinate of the service"),
                        fieldWithPath("status").description("Service status"),
                        fieldWithPath("createdAt").description("Creation timestamp"),
                        fieldWithPath("updatedAt").description("Last update timestamp"),
                    ),
                ),
            )
    }

    @Test
    fun `데이터베이스 서비스 생성`() {
        val workspace = Workspace(name = "Test Workspace")
        val project = Project(name = "Project Alpha", subdomain = "alpha", workspace = workspace)

        val request =
            CreateDatabaseServiceRequest(
                name = "Test Database",
                x = 0.1,
                y = 0.1,
                engine = DatabaseEngine.MYSQL,
            )

        val databaseService =
            DatabaseService(
                name = "Test Database",
                x = 0.1,
                y = 0.1,
                project = project,
                status = ServiceStatus.RUNNING,
                engine = DatabaseEngine.MYSQL,
                version = "8.0.39",
                database = "test_database",
                username = "test_user",
                password = "test_password",
            )

        every {
            serviceService.createDatabaseService(
                testUserId,
                testWorkspaceId,
                testProjectId,
                CreateDatabaseService(
                    name = "Test Database",
                    x = 0.1,
                    y = 0.1,
                    engine = DatabaseEngine.MYSQL,
                ),
            )
        } returns databaseService

        mockMvc
            .perform(
                post(
                    "/api/v1/workspaces/{workspaceId}/projects/{projectId}/services/database",
                    testWorkspaceId,
                    testProjectId,
                ).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "service-create-database",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                        parameterWithName("projectId").description("Project ID"),
                    ),
                    requestFields(
                        fieldWithPath("name").description("Service name"),
                        fieldWithPath("x").description("X coordinate of the service"),
                        fieldWithPath("y").description("Y coordinate of the service"),
                        fieldWithPath("engine").description("Engine of the database"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("Service ID"),
                        fieldWithPath("name").description("Service name"),
                        fieldWithPath("x").description("X coordinate of the service"),
                        fieldWithPath("y").description("Y coordinate of the service"),
                        fieldWithPath("status").description("Service status"),
                        fieldWithPath("createdAt").description("Creation timestamp"),
                        fieldWithPath("updatedAt").description("Last update timestamp"),
                    ),
                ),
            )
    }

    @Test
    fun `서비스 삭제`() {
        val serviceId = UUID.randomUUID()

        every { serviceService.deleteService(testUserId, testWorkspaceId, testProjectId, serviceId) } returns Unit

        mockMvc
            .perform(
                delete(
                    "/api/v1/workspaces/{workspaceId}/projects/{projectId}/services/{serviceId}",
                    testWorkspaceId,
                    testProjectId,
                    serviceId,
                ),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "service-delete",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                        parameterWithName("projectId").description("Project ID"),
                        parameterWithName("serviceId").description("Service ID to delete"),
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
