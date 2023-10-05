package com.hedvig.android.hanalytics

import android.content.Context
import android.os.Build
import com.hedvig.android.core.common.android.jsonObjectOf
import com.hedvig.android.core.common.android.plus
import com.hedvig.android.core.common.await
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

internal class AndroidHAnalyticsService(
  private val context: Context,
  private val okHttpClient: OkHttpClient,
  private val deviceIdDataStore: DeviceIdDataStore,
  private val hAnalyticsBaseUrl: String,
  private val appVersionName: String,
  private val appVersionCode: String,
  private val appId: String,
) : HAnalyticsService {

  private val sessionId = UUID.randomUUID()

  override suspend fun getExperiments(): List<Experiment>? {
    val requestJsonObject = contextProperties() + jsonObjectOf(
      "appName" to "android",
      "appVersion" to appVersionName,
      "filter" to HAnalytics.EXPERIMENTS,
    )
    val experimentRequest = Request.Builder()
      .url("$hAnalyticsBaseUrl/experiments")
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

  private suspend fun deviceId() = deviceIdDataStore.observeDeviceId().firstOrNull() ?: ""

  private suspend fun contextProperties() = jsonObjectOf(
    "app" to jsonObjectOf(
      "name" to context.applicationInfo.loadLabel(context.packageManager).toString(),
      "version" to appVersionName,
      "build" to appVersionCode,
      "namespace" to appId,
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
