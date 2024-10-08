package com.anifichadia.figstract.util

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileManagement {
    fun outDirectory(outPath: String): File {
        return Paths.get("", outPath).toFile()
    }

    private fun tempDirectoryPath(): Path {
        return Paths.get("", "temp").also { path ->
            path.toFile().also {
                it.mkdirs()
                it.deleteOnExit()
            }
        }
    }

    fun stepCreateTempFile(
        stepName: String,
        prefix: String? = null,
        suffix: String? = null,
    ): Path {
        return Files.createTempFile(
            /* dir = */ stepTempDirectoryPath(stepName),
            /* prefix = */ prefix,
            /* suffix = */ suffix,
        ).also {
            it.toFile().deleteOnExit()
        }
    }

    private fun stepTempDirectoryPath(stepName: String): Path {
        return tempDirectoryPath().resolve(stepName).also { path ->
            path.toFile().also {
                it.mkdirs()
                it.deleteOnExit()
            }
        }
    }
}
