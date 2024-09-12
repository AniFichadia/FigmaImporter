package com.anifichadia.figstract.cli.core.assets

import com.anifichadia.figstract.HttpClientFactory
import com.anifichadia.figstract.cli.core.outDirectory
import com.anifichadia.figstract.figma.api.FigmaApi
import com.anifichadia.figstract.importer.asset.FigmaAssetImporter
import com.anifichadia.figstract.importer.asset.model.AssetFileHandler
import com.anifichadia.figstract.model.tracking.JsonFileProcessingRecordRepository
import com.anifichadia.figstract.model.tracking.NoOpProcessingRecordRepository
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.findObject
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.sources.PropertiesValueSource
import io.ktor.client.engine.ProxyConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.io.File

abstract class AssetsCommand : CliktCommand(
    name = "assets",
    help = """
        Extracts assets from figma files, such as images and icons
    """.trimIndent(),
    printHelpOnEmptyArgs = true,
) {
    init {
        context {
            valueSources(
                PropertiesValueSource.from("$commandName.properties"),
            )
        }
    }

    private val proxyConfig by findObject<ProxyConfig>()
    private val figmaApi by requireObject<FigmaApi>()

    private val trackingEnabled: Boolean by option("--trackingEnabled")
        .boolean()
        .default(true)

    private val outDirectory by outDirectory()

    abstract fun createHandlers(outDirectory: File): List<AssetFileHandler>

    override fun run() = runBlocking {
        val downloaderHttpClient = HttpClientFactory.downloader(
            proxy = proxyConfig,
        )

        val processingRecordRepository = if (trackingEnabled) {
            JsonFileProcessingRecordRepository(
                recordFile = File(outDirectory, "processing_record.json")
            )
        } else {
            NoOpProcessingRecordRepository
        }

        val importer = FigmaAssetImporter(
            figmaApi = figmaApi,
            downloaderHttpClient = downloaderHttpClient,
            processingRecordRepository = processingRecordRepository,
        )
        coroutineScope {
            importer.importFromFigma(
                handlers = createHandlers(outDirectory),
            )
        }
    }
}
