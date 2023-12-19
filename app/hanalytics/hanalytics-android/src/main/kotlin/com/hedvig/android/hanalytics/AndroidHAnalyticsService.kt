package com.hedvig.android.hanalytics

import android.content.Context
import android.os.Build
import com.hedvig.android.core.common.await
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.hanalytics.HAnalytics
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

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
    val requestJsonObject = JsonObject(
      contextProperties() + buildJsonObject {
        put("appName", JsonPrimitive("android"))
        put("appVersion", JsonPrimitive(appVersionName))
        put("filter", JsonArray(HAnalytics.EXPERIMENTS.map { JsonPrimitive(it) }))
      },
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

  private suspend fun contextProperties(): JsonObject = buildJsonObject {
    put(
      "app",
      buildJsonObject {
        put("name", JsonPrimitive(context.applicationInfo.loadLabel(context.packageManager).toString()))
        put("version", JsonPrimitive(appVersionName))
        put("build", JsonPrimitive(appVersionCode))
        put("namespace", JsonPrimitive(appId))
      },
    )
    put(
      "device",
      buildJsonObject {
        put("manufacturer", JsonPrimitive(Build.MANUFACTURER))
        put("type", JsonPrimitive("android"))
        put("model", JsonPrimitive(Build.MODEL))
        put("name", JsonPrimitive(Build.DEVICE))
      },
    )
    put(
      "os",
      buildJsonObject {
        put("name", JsonPrimitive("Android"))
        put("version", JsonPrimitive(Build.VERSION.RELEASE))
      },
    )
    put("screen", buildJsonObject { })
    put("userAgent", JsonPrimitive(System.getProperty("http.agent") ?: "undefined"))
    put("locale", JsonPrimitive("${Locale.getDefault().language}-${Locale.getDefault().country}"))
    put("timezone", JsonPrimitive(TimeZone.getDefault()?.id ?: "undefined"))
    put("sessionId", JsonPrimitive(sessionId.toString()))
    put("trackingId", JsonPrimitive(deviceId()))
  }
}
