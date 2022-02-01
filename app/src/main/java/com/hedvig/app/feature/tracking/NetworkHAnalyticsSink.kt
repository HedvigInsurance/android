package com.hedvig.app.feature.tracking

import android.content.Context
import android.os.Build
import com.hedvig.app.BuildConfig
import com.hedvig.app.util.jsonObjectOf
import com.hedvig.app.util.plus
import com.hedvig.hanalytics.HAnalyticsEvent
import e
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class NetworkHAnalyticsSink(
    private val okHttpClient: OkHttpClient,
    private val context: Context,
    private val baseUrl: String,
) : HAnalyticsSink {
    private val sessionId = UUID.randomUUID()
    override fun send(event: HAnalyticsEvent) {
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
                                    "trackingId" to "TODO", // TODO: APP-1216
                                    "sessionId" to sessionId,
                                    "graphql" to event.graphql,
                                )
                                ).toString().toRequestBody()
                        )
                        .build()
                ).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                    }
                })
        }
    }

    private fun contextProperties() = jsonObjectOf(
        "app" to jsonObjectOf(
            "name" to context.applicationInfo.loadLabel(context.packageManager).toString(),
            "version" to BuildConfig.VERSION_NAME,
            "build" to BuildConfig.VERSION_CODE,
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
