package com.hedvig.android.datadog.core

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.datadog.android.Datadog
import com.datadog.android.DatadogSite
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.log.Logger
import com.datadog.android.log.Logs
import com.datadog.android.log.LogsConfiguration
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.Rum
import com.datadog.android.rum.RumConfiguration
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy
import com.datadog.android.trace.AndroidTracer
import com.datadog.android.trace.Trace
import com.datadog.android.trace.TraceConfiguration
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.opentracing.util.GlobalTracer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

// Used in /app/src/main/AndroidManifest.xml
abstract class DatadogInitializer : Initializer<Unit>, KoinComponent {
  private val hedvigBuildConstants by inject<HedvigBuildConstants>()

  override fun create(context: Context) {
    val clientToken = "pub185bcba7ed324e83d068b80e25a81359"
    val applicationId = "4d7b8355-396d-406e-b543-30a073050e8f"

    val environmentName = if (hedvigBuildConstants.isProduction) "prod" else "dev"

    val configuration = Configuration
      .Builder(
        clientToken = clientToken,
        env = environmentName,
        service = "android",
      )
      .useSite(DatadogSite.EU1)
      .setFirstPartyHosts(listOf(hedvigBuildConstants.urlGraphqlOctopus.removePrefix("https://")))
      .build()
    val sdkCore = Datadog.initialize(context, configuration, TrackingConsent.GRANTED)
    if (sdkCore == null) {
      logcat(LogPriority.ERROR) { "Initializing datadog failed!" }
      return
    }
    if (hedvigBuildConstants.isDebug) {
      Datadog.setVerbosity(Log.VERBOSE)
    }

    val rumConfig = RumConfiguration.Builder(applicationId)
      .trackLongTasks()
      .trackFrustrations(true)
      .useViewTrackingStrategy(ActivityViewTrackingStrategy(true))
      .build()
    Rum.enable(rumConfig, sdkCore)
    logcat(LogPriority.VERBOSE) { "Datadog RUM registering succeeded: true" }

    val traceConfiguration = TraceConfiguration.Builder().build()
    Trace.enable(traceConfiguration, sdkCore)
    logcat(LogPriority.VERBOSE) { "Datadog Tracer registering succeeded: true" }
    val androidTracer = AndroidTracer.Builder()
      .setService("android")
      .setBundleWithRumEnabled(true)
      .build()
    val didRegisterGlobalTracer = GlobalTracer.registerIfAbsent { androidTracer }
    logcat(LogPriority.VERBOSE) { "Datadog Android Global Tracer registering succeeded: $didRegisterGlobalTracer" }

    val logsConfig = LogsConfiguration.Builder().build()
    Logs.enable(logsConfig, sdkCore)

    val logger = Logger.Builder(sdkCore)
      .setNetworkInfoEnabled(true)
      .setBundleWithRumEnabled(true)
      .setBundleWithTraceEnabled(true)
      .setRemoteSampleRate(100f)
      .setService("android")
      .setRemoteLogThreshold(Log.DEBUG)
      .apply {
        if (hedvigBuildConstants.isDebug) {
          setLogcatLogsEnabled(true)
        }
      }
      .build()

    Timber.plant(DatadogLoggingTree(logger))
  }
}
