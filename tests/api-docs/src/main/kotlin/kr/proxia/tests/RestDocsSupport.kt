package kr.proxia.tests

import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

object RestDocsSupport {
    fun mockMvc(
        context: WebApplicationContext,
        restDocumentation: RestDocumentationContextProvider,
    ): MockMvc =
        MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(documentationConfiguration(restDocumentation))
            .build()
}
