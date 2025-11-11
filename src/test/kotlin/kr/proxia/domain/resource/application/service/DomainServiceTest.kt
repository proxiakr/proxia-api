package kr.proxia.domain.resource.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.proxia.domain.resource.domain.entity.DomainResourceEntity
import kr.proxia.domain.resource.domain.repository.DomainResourceRepository
import kr.proxia.global.nginx.properties.NginxProperties
import java.util.Optional

class DomainServiceTest :
    BehaviorSpec({
        val domainResourceRepository = mockk<DomainResourceRepository>()
        val nginxProperties = mockk<NginxProperties>()

        val domainService =
            DomainService(
                domainResourceRepository,
                nginxProperties,
            )

        Given("generateSubdomain") {
            val slug = "test-project"
            val baseDomain = "proxia.kr"

            When("서브도메인이 사용 가능한 경우") {
                every { nginxProperties.baseDomain } returns baseDomain
                every { domainResourceRepository.findBySubdomainAndDeletedAtIsNull("$slug.$baseDomain") } returns null

                val result = domainService.generateSubdomain(slug)

                Then("기본 서브도메인 반환") {
                    result shouldBe "$slug.$baseDomain"
                }
            }

            When("서브도메인이 이미 사용 중인 경우") {
                val existingDomain = mockk<DomainResourceEntity>(relaxed = true)

                every { nginxProperties.baseDomain } returns baseDomain
                every { domainResourceRepository.findBySubdomainAndDeletedAtIsNull("$slug.$baseDomain") } returns existingDomain
                every {
                    domainResourceRepository.findBySubdomainAndDeletedAtIsNull(
                        "$slug-1.$baseDomain",
                    )
                } returns null

                val result = domainService.generateSubdomain(slug)

                Then("카운터가 추가된 서브도메인 반환") {
                    result shouldBe "$slug-1.$baseDomain"
                }
            }

            When("서브도메인이 여러 개 사용 중인 경우") {
                val existingDomain = mockk<DomainResourceEntity>(relaxed = true)

                every { nginxProperties.baseDomain } returns baseDomain
                every { domainResourceRepository.findBySubdomainAndDeletedAtIsNull("$slug.$baseDomain") } returns existingDomain
                every {
                    domainResourceRepository.findBySubdomainAndDeletedAtIsNull(
                        "$slug-1.$baseDomain",
                    )
                } returns existingDomain
                every {
                    domainResourceRepository.findBySubdomainAndDeletedAtIsNull(
                        "$slug-2.$baseDomain",
                    )
                } returns null

                val result = domainService.generateSubdomain(slug)

                Then("사용 가능한 서브도메인 반환") {
                    result shouldBe "$slug-2.$baseDomain"
                }
            }
        }

        Given("isSubdomainTaken") {
            val subdomain = "test.proxia.kr"

            When("서브도메인이 사용 중인 경우") {
                val existingDomain = mockk<DomainResourceEntity>(relaxed = true)
                every { domainResourceRepository.findBySubdomainAndDeletedAtIsNull(subdomain) } returns existingDomain

                val result = domainService.isSubdomainTaken(subdomain)

                Then("true 반환") {
                    result shouldBe true
                }
            }

            When("서브도메인이 사용 가능한 경우") {
                every { domainResourceRepository.findBySubdomainAndDeletedAtIsNull(subdomain) } returns null

                val result = domainService.isSubdomainTaken(subdomain)

                Then("false 반환") {
                    result shouldBe false
                }
            }
        }

        Given("isCustomDomainTaken") {
            val customDomain = "example.com"

            When("커스텀 도메인이 사용 중인 경우") {
                val existingDomain = mockk<DomainResourceEntity>(relaxed = true)
                every {
                    domainResourceRepository.findByCustomDomainAndDeletedAtIsNull(
                        customDomain,
                    )
                } returns existingDomain

                val result = domainService.isCustomDomainTaken(customDomain)

                Then("true 반환") {
                    result shouldBe true
                }
            }

            When("커스텀 도메인이 사용 가능한 경우") {
                every {
                    domainResourceRepository.findByCustomDomainAndDeletedAtIsNull(
                        customDomain,
                    )
                } returns null

                val result = domainService.isCustomDomainTaken(customDomain)

                Then("false 반환") {
                    result shouldBe false
                }
            }
        }

        Given("generateVerificationToken") {
            When("토큰 생성") {
                val token1 = domainService.generateVerificationToken()
                val token2 = domainService.generateVerificationToken()

                Then("고유한 UUID 반환") {
                    token1.length shouldBe 36 // UUID format
                    token2.length shouldBe 36
                    (token1 != token2) shouldBe true
                }
            }
        }

        Given("getFullDomain") {
            When("도메인 리소스가 null인 경우") {
                val result = domainService.getFullDomain(null)

                Then("null 반환") {
                    result shouldBe null
                }
            }

            When("커스텀 도메인이 인증된 경우") {
                val domainResource = mockk<DomainResourceEntity>(relaxed = true)
                every { domainResource.customDomain } returns "example.com"
                every { domainResource.verified } returns true
                every { domainResource.subdomain } returns "test.proxia.kr"

                val result = domainService.getFullDomain(domainResource)

                Then("커스텀 도메인 반환") {
                    result shouldBe "example.com"
                }
            }

            When("커스텀 도메인이 인증되지 않은 경우") {
                val domainResource = mockk<DomainResourceEntity>(relaxed = true)
                every { domainResource.customDomain } returns "example.com"
                every { domainResource.verified } returns false
                every { domainResource.subdomain } returns "test.proxia.kr"

                val result = domainService.getFullDomain(domainResource)

                Then("서브도메인 반환") {
                    result shouldBe "test.proxia.kr"
                }
            }

            When("커스텀 도메인이 없는 경우") {
                val domainResource = mockk<DomainResourceEntity>(relaxed = true)
                every { domainResource.customDomain } returns null
                every { domainResource.subdomain } returns "test.proxia.kr"

                val result = domainService.getFullDomain(domainResource)

                Then("서브도메인 반환") {
                    result shouldBe "test.proxia.kr"
                }
            }
        }

        Given("verifyCustomDomain") {
            val domainResourceId = 1L
            val verificationToken = "test-token"

            When("도메인 리소스를 찾을 수 없는 경우") {
                every { domainResourceRepository.findById(domainResourceId) } returns Optional.empty()

                val result = domainService.verifyCustomDomain(domainResourceId, verificationToken)

                Then("false 반환") {
                    result shouldBe false
                }
            }

            When("커스텀 도메인이 설정되지 않은 경우") {
                val domainResource = mockk<DomainResourceEntity>(relaxed = true)
                every { domainResource.customDomain } returns null

                every { domainResourceRepository.findById(domainResourceId) } returns Optional.of(domainResource)

                val result = domainService.verifyCustomDomain(domainResourceId, verificationToken)

                Then("false 반환") {
                    result shouldBe false
                }
            }
        }
    })
