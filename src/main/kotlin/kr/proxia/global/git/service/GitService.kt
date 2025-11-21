package kr.proxia.global.git.service

import kr.proxia.global.git.response.GitCommitResponse
import org.springframework.stereotype.Service
import java.io.File

@Service
interface GitService {
    fun getLatestCommit(
        owner: String,
        repo: String,
        branch: String,
        token: String,
    ): GitCommitResponse

    fun clone(
        repositoryUrl: String,
        branch: String,
        targetDir: File,
        token: String? = null,
    ): File
}
