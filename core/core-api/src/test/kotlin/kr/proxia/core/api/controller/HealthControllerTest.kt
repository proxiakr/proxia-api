package kr.proxia.core.api.controller

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class HealthControllerTest :
    FunSpec({
        test("health returns UP status") {
            val controller = HealthController()
            val result = controller.health()
            result["status"] shouldBe "UP"
        }
    })
