package kr.proxia.core.api.controller.v1

import io.mockk.every
import io.mockk.mockk
import kr.proxia.core.domain.GitRepository
import kr.proxia.core.domain.GitRepositoryService
import kr.proxia.core.support.OffsetLimit
import kr.proxia.core.support.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.MethodParameter
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
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
import java.util.UUID

@Tag("restdocs")
@ExtendWith(RestDocumentationExtension::class)
class GitRepositoryControllerDocsTest {
    private lateinit var mockMvc: MockMvc

    private val gitRepositoryService = mockk<GitRepositoryService>()
    private val testUserId: UUID = UUID.randomUUID()
    private val testWorkspaceId: UUID = UUID.randomUUID()
    private val testGitConnectionId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(GitRepositoryController(gitRepositoryService))
                .setCustomArgumentResolvers(TestAuthenticationPrincipalResolver(testUserId))
                .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
                .build()
    }

    @Test
    fun `Git repository 목록 조회`() {
        val repository1 = GitRepository(name = "repository-1")
        val repository2 = GitRepository(name = "repository-2")

        every {
            gitRepositoryService.getRepositories(
                testUserId,
                testWorkspaceId,
                testGitConnectionId,
                OffsetLimit(0, 2),
            )
        } returns
            Page(
                content = listOf(repository1, repository2),
                hasNext = false,
            )

        mockMvc
            .perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/git/connections/{connectionId}/repositories",
                    testWorkspaceId,
                    testGitConnectionId,
                ).queryParam("offset", "0")
                    .queryParam("limit", "2"),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "git-repository-list",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("workspaceId").description("Workspace ID"),
                        parameterWithName("connectionId").description("Connection ID"),
                    ),
                    responseFields(
                        fieldWithPath("content[].name").description("Git Repository Name"),
                        fieldWithPath("hasNext").description("Whether more repositories exist"),
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
