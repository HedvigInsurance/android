package com.hedvig.android.datadog

import android.content.Context
import androidx.startup.Initializer
import com.datadog.android.Datadog
import com.datadog.android.DatadogSite
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumMonitor
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy
import com.datadog.android.tracing.AndroidTracer
import com.hedvig.android.core.common.di.isDebugQualifier
import com.hedvig.android.core.common.di.isProductionQualifier
import io.opentracing.util.GlobalTracer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import slimber.log.v
import timber.log.Timber

@Suppress("unused") // Used in /app/src/main/AndroidManifest.xml
abstract class DatadogInitializer : Initializer<Unit>, KoinComponent {

  private val isDebug by inject<Boolean>(isDebugQualifier)
  private val isProduction by inject<Boolean>(isProductionQualifier)

  @Suppress("KotlinConstantConditions")
  override fun create(context: Context) {
    val clientToken = "pub185bcba7ed324e83d068b80e25a81359"
    val applicationId = "4d7b8355-396d-406e-b543-30a073050e8f"

    val environmentName = if (isProduction) "prod" else "dev"
    val configuration = Configuration.Builder(
      logsEnabled = true,
      tracesEnabled = true,
      crashReportsEnabled = true,
      rumEnabled = true,
    )
      .useSite(DatadogSite.EU1)
      .trackInteractions()
      .trackLongTasks(300)
      .setFirstPartyHosts(listOf("app.datadoghq.eu"))
      .useViewTrackingStrategy(ActivityViewTrackingStrategy(true))
      .build()

    val credentials = Credentials(
      clientToken = clientToken,
      envName = environmentName,
      variant = Credentials.NO_VARIANT,
      rumApplicationId = applicationId,
      serviceName = "android",
    )
    if (isDebug) {
      Datadog.setVerbosity(android.util.Log.VERBOSE)
    }
    Datadog.initialize(context, credentials, configuration, TrackingConsent.GRANTED)
    val didRegisterGlobalRum = GlobalRum.registerIfAbsent {
      RumMonitor.Builder().build()
    }
    v { "Datadog RUM registering succeeded: $didRegisterGlobalRum" }
    val didRegisterGlobalTracer = GlobalTracer.registerIfAbsent {
      AndroidTracer.Builder().build()
    }
    v { "Datadog Global Tracer registering succeeded: $didRegisterGlobalTracer" }

    Timber.plant(DatadogLoggingTree())
  }
}
