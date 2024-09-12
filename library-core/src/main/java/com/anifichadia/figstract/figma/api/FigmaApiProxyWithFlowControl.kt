package com.anifichadia.figstract.figma.api

import com.anifichadia.figstract.apiclient.ApiResponse
import com.anifichadia.figstract.figma.FileKey
import com.anifichadia.figstract.figma.api.KnownErrors.errorMatches
import com.anifichadia.figstract.figma.model.ExportSetting
import com.anifichadia.figstract.figma.model.GetFilesResponse
import com.anifichadia.figstract.figma.model.GetImageResponse
import com.anifichadia.figstract.figma.model.GetLocalVariablesResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FigmaApiProxyWithFlowControl(
    private val actualApi: FigmaApi,
    concurrencyLimit: Int = DEFAULT_CONCURRENCY_LIMIT,
    private val retryLimit: Int = 5,
    /** The figma API may throttle requests, so we unfortunately need a steep backoff curve to improve reliability */
    private val throttleDelayDurations: List<Duration> = listOf(
        1.seconds,
        5.seconds,
        15.seconds,
        30.seconds,
        1.minutes,
    ),
) : FigmaApi {
    private val concurrencySemaphore = Semaphore(concurrencyLimit)
    private val floodMitigationMutex = Mutex()

    override suspend fun getFile(key: FileKey): ApiResponse<GetFilesResponse> {
        return wrapRequest {
            actualApi.getFile(key = key)
        }
    }

    override suspend fun getImages(
        key: FileKey,
        ids: List<String>,
        format: ExportSetting.Format,
        scale: Float,
        contentsOnly: Boolean?,
    ): ApiResponse<GetImageResponse> {
        return wrapRequest {
            actualApi.getImages(
                key = key,
                ids = ids,
                format = format,
                scale = scale,
                contentsOnly = contentsOnly,
            )
        }
    }

    override suspend fun getLocalVariables(
        key: FileKey,
    ): ApiResponse<GetLocalVariablesResponse> {
        return wrapRequest {
            actualApi.getLocalVariables(
                key = key,
            )
        }
    }

    private suspend inline fun <V> wrapRequest(block: () -> ApiResponse<V>): ApiResponse<V> {
        concurrencySemaphore.withPermit {
            var attempt = 1
            lateinit var lastApiResponse: ApiResponse<V>
            while (attempt <= retryLimit) {
                floodMitigationMutex.withLock {
                    // Just await the mutex unlocking
                }

                lastApiResponse = block()

                val isRateLimitError = lastApiResponse.errorMatches(KnownErrors.rateLimitExceeded)
                if (!isRateLimitError) {
                    return lastApiResponse
                } else if (!floodMitigationMutex.isLocked) {
                    floodMitigationMutex.withLock {
                        delay(throttleDelayDurations[(attempt - 1).coerceIn(throttleDelayDurations.indices)])
                    }
                }

                attempt += 1
            }

            return lastApiResponse
        }
    }

    companion object {
        const val DEFAULT_CONCURRENCY_LIMIT = 5
    }
}