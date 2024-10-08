package com.anifichadia.figstract.importer.asset.model.importing

import com.anifichadia.figstract.importer.asset.model.Instruction
import com.anifichadia.figstract.importer.asset.model.importing.ImportPipeline.Step.Companion.resolveExtension
import com.anifichadia.figstract.importer.asset.model.importing.ImportPipeline.Step.Companion.resolveOutputName
import com.anifichadia.figstract.importer.asset.model.importing.ImportPipeline.Step.Companion.resolvePathElements
import com.anifichadia.figstract.model.Describeable
import com.anifichadia.figstract.type.fold
import java.io.File

/**
 * Used to save the output of an [ImportPipeline]. This is a specialised, finalising version of
 * [ImportPipeline.Step].
 */
abstract class Destination : ImportPipeline.Step {
    abstract suspend fun write(
        instruction: Instruction,
        input: ImportPipeline.Output,
    )

    final override suspend fun process(
        instruction: Instruction,
        input: ImportPipeline.Output,
    ): List<ImportPipeline.Output> {
        write(instruction, input)

        return ImportPipeline.Output.none
    }

    /** Black hole */
    object None : Destination(), Describeable {
        override suspend fun write(instruction: Instruction, input: ImportPipeline.Output) {
            // No-op
        }

        override fun describe(): String {
            return "Destination.none"
        }

        override fun toString(): String {
            return describe()
        }
    }

    /** @see [Destination.directoryDestination] */
    class Directory(
        private val directory: File,
    ) : Destination(), Describeable {
        override suspend fun write(
            instruction: Instruction,
            input: ImportPipeline.Output,
        ) {
            val data = input.data

            val outputName = resolveOutputName(instruction, input)
            val outputPathElements = resolvePathElements(instruction, input)
            val extension = resolveExtension(instruction, input)

            val outputFile = directory.fold(outputPathElements, "$outputName.$extension")
            outputFile.parentFile.mkdirs()

            outputFile.writeBytes(data)
        }

        override fun describe(): String {
            return "Destination.directory(directory: $directory)"
        }

        override fun toString(): String {
            return describe()
        }
    }

    companion object {
        fun directoryDestination(directory: File): Directory {
            return Directory(
                directory = directory,
            )
        }
    }
}
