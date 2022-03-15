package com.hedvig.app.feature.tracking

import android.content.Context
import android.os.Build
import com.hedvig.app.BuildConfig
import com.hedvig.app.authenticate.DeviceIdStore
import com.hedvig.app.util.jsonObjectOf
import com.hedvig.app.util.plus
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.okhttp.await

class NetworkHAnalyticsSink(
    private val okHttpClient: OkHttpClient,
    private val context: Context,
    private val deviceIdStore: DeviceIdStore,
    private val baseUrl: String,
) : HAnalyticsSink, ExperimentProvider {
    private val sessionId = UUID.randomUUID()
    override fun send(event: HAnalyticsEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                okHttpClient
                    .newCall(
                        Request.Builder()
                            .url("$baseUrl/event")
                            .header("Content-Type", "application/json")
                            .post(
                                (
                                    contextProperties() + jsonObjectOf(
                                        "event" to event.name,
                                        "properties" to event.properties,
                                        "trackingId" to deviceId(),
                                        "sessionId" to sessionId,
                                        "graphql" to event.graphql,
                                    )
                                    ).toString().toRequestBody()
                            )
                            .build()
                    )
                    .await()
            }
        }
    }

    private suspend fun deviceId() = deviceIdStore.observeDeviceId().firstOrNull() ?: ""

    private fun contextProperties() = jsonObjectOf(
        "app" to jsonObjectOf(
            "name" to context.applicationInfo.loadLabel(context.packageManager).toString(),
            "version" to BuildConfig.VERSION_NAME,
            "build" to BuildConfig.VERSION_CODE.toString(),
            "namespace" to BuildConfig.APPLICATION_ID,
        ),
        "device" to jsonObjectOf(
            "manufacturer" to Build.MANUFACTURER,
            "type" to "android",
            "model" to Build.MODEL,
            "name" to Build.DEVICE,
        ),
        "os" to jsonObjectOf(
            "name" to "Android",
            "version" to Build.VERSION.RELEASE,
        ),
        "screen" to jsonObjectOf(),
        "userAgent" to (System.getProperty("http.agent") ?: "undefined"),
        "locale" to "${Locale.getDefault().language}-${Locale.getDefault().country}",
        "timezone" to (TimeZone.getDefault()?.id ?: "undefined"),
    )

    private val mutex = Mutex()
    private val experimentsData = mutableMapOf<String, String>()

    override suspend fun getExperiment(name: String): HAnalyticsExperiment {
        return mutex.withLock {
            if (experimentsData.isEmpty()) {
                loadExperimentsFromServer()
            }

            experimentsData[name]?.let { variant ->
                HAnalyticsExperiment(name, variant)
            } ?: throw Exception("experiment unavailable")
        }
    }

    override suspend fun invalidateExperiments() {
        mutex.withLock {
            experimentsData.clear()
        }
    }

    private suspend fun loadExperimentsFromServer() {
        withContext(Dispatchers.IO) {
            val result = okHttpClient
                .newCall(
                    Request.Builder()
                        .url("$baseUrl/experiments")
                        .header("Content-Type", "application/json")
                        .post(
                            (
                                contextProperties() + jsonObjectOf(
                                    "trackingId" to deviceId(),
                                    "sessionId" to sessionId,
                                    "appName" to "android",
                                    "appVersion" to BuildConfig.VERSION_NAME,
                                    "filter" to HAnalytics.EXPERIMENTS,
                                )
                                ).toString().toRequestBody()
                        )
                        .build()
                ).await()

            val responseBody = result.body?.string() ?: return@withContext
            val responseParsed = Json.decodeFromString<List<Experiment>>(responseBody)

            experimentsData.clear()
            experimentsData.putAll(responseParsed.map { it.name to it.variant })
            send(
                HAnalyticsEvent(
                    "experiments_loaded",
                    mapOf("experiments" to experimentsData),
                )
            )
        }
    }
}

@Serializable
data class Experiment(
    val name: String,
    val variant: String,
)
