package kr.proxia.global.git.service

import org.eclipse.jgit.api.Git
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

@Service
class GitService {
    fun getCommitSha(
        gitUrl: String,
        branch: String,
    ): String {
        val tempDir = Files.createTempDirectory("git-").toFile()
        try {
            Git
                .lsRemoteRepository()
                .setRemote(gitUrl)
                .setHeads(true)
                .call()
                .find { it.name == "refs/heads/$branch" }
                ?.objectId
                ?.name
                ?: throw IllegalArgumentException("Branch $branch not found")
        } finally {
            tempDir.deleteRecursively()
        }.let { return it }
    }

    fun clone(
        gitUrl: String,
        branch: String,
        targetDir: File,
    ): File {
        Git
            .cloneRepository()
            .setURI(gitUrl)
            .setBranch(branch)
            .setDirectory(targetDir)
            .setDepth(1)
            .call()
            .close()

        return targetDir
    }

    fun validateRepository(gitUrl: String): Boolean =
        runCatching {
            Git
                .lsRemoteRepository()
                .setRemote(gitUrl)
                .call()
            true
        }.getOrDefault(false)
}
