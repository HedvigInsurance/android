package com.hedvig.app.feature.tracking

import android.content.Context
import android.os.Build
import com.hedvig.app.BuildConfig
import com.hedvig.app.authenticate.DeviceIdStore
import com.hedvig.app.util.jsonObjectOf
import com.hedvig.app.util.plus
import com.hedvig.hanalytics.HAnalyticsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.okhttp.await
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class NetworkHAnalyticsSink(
    private val okHttpClient: OkHttpClient,
    private val context: Context,
    private val deviceIdStore: DeviceIdStore,
    private val baseUrl: String,
) : HAnalyticsSink {
    private val sessionId = UUID.randomUUID()
    override fun send(event: HAnalyticsEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val deviceId = deviceIdStore.observeDeviceId().firstOrNull() ?: ""
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
                                        "trackingId" to deviceId,
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
}
