package kr.proxia.core.api.controller.v1.response

import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.storage.db.core.entity.AppService
import kr.proxia.storage.db.core.entity.DatabaseService
import kr.proxia.storage.db.core.entity.Service

data class ServiceTypeResponse(
    val category: String,
    val techStack: String,
) {
    companion object {
        fun from(service: Service) =
            when (service) {
                is AppService -> ServiceTypeResponse(
                    category = "app",
                    techStack = service.framework.name,
                )

                is DatabaseService -> ServiceTypeResponse(
                    category = "database",
                    techStack = service.engine.name,
                )

                else -> throw CoreException(ErrorType.UNSUPPORTED_SERVICE_TYPE)
            }
    }
}
