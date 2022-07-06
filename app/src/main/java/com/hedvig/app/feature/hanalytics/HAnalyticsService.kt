package com.hedvig.app.feature.hanalytics

import android.content.Context
import android.os.Build
import com.hedvig.android.core.jsonObjectOf
import com.hedvig.android.core.plus
import com.hedvig.app.BuildConfig
import com.hedvig.app.authenticate.DeviceIdStore
import com.hedvig.app.util.coroutines.await
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

interface HAnalyticsService {
  suspend fun sendEvent(event: HAnalyticsEvent)
  suspend fun getExperiments(): List<Experiment>?
  suspend fun identify()
}

class HAnalyticsServiceImpl(
  private val context: Context,
  private val okHttpClient: OkHttpClient,
  private val deviceIdStore: DeviceIdStore,
  private val baseUrl: String,
) : HAnalyticsService {

  @Suppress("BlockingMethodInNonBlockingContext")
  override suspend fun sendEvent(event: HAnalyticsEvent) {
    val requestJsonObject = contextProperties() + jsonObjectOf(
      "event" to event.name,
      "properties" to event.properties,
      "graphql" to event.graphql,
    )
    val eventRequest = Request.Builder()
      .url("$baseUrl/event")
      .header("Content-Type", "application/json")
      .post(requestJsonObject.toString().toRequestBody())
      .build()
    withContext(Dispatchers.IO) {
      try {
        okHttpClient.newCall(eventRequest).execute()
      } catch (ignored: IOException) {
      }
    }
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  override suspend fun getExperiments(): List<Experiment>? {
    val requestJsonObject = contextProperties() + jsonObjectOf(
      "appName" to "android",
      "appVersion" to BuildConfig.VERSION_NAME,
      "filter" to HAnalytics.EXPERIMENTS,
    )
    val experimentRequest = Request.Builder()
      .url("$baseUrl/experiments")
      .header("Content-Type", "application/json")
      .post(requestJsonObject.toString().toRequestBody())
      .build()
    return withContext(Dispatchers.IO) {
      try {
        val response = okHttpClient.newCall(experimentRequest).await()
        val responseString = response.body?.string() ?: return@withContext null
        Json.decodeFromString<List<Experiment>>(responseString)
      } catch (ignored: IOException) {
        null
      }
    }
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  override suspend fun identify() {
    val requestJsonObject = jsonObjectOf("trackingId" to deviceId())

    val request = Request.Builder()
      .url("$baseUrl/identify")
      .header("Content-Type", "application/json")
      .post(requestJsonObject.toString().toRequestBody())
      .build()

    withContext(Dispatchers.IO) {
      try {
        okHttpClient.newCall(request).await()
      } catch (ignored: IOException) {
      }
    }
  }

  private val sessionId = UUID.randomUUID()
  private suspend fun deviceId() = deviceIdStore.observeDeviceId().firstOrNull() ?: ""

  private suspend fun contextProperties() = jsonObjectOf(
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
    "sessionId" to sessionId,
    "trackingId" to deviceId(),
  )
}
