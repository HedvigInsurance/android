package com.hedvig.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import com.apollographql.apollo3.ApolloClient
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumMonitor
import com.hedvig.android.hanalytics.android.tracking.ApplicationLifecycleTracker
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.util.FirebaseCrashlyticsLogExceptionTree
import org.koin.android.ext.android.inject
import timber.log.Timber

open class HedvigApplication : Application() {
  protected val apolloClient: ApolloClient by inject()
  private val whatsNewRepository: WhatsNewRepository by inject()
  private val applicationLifecycleTracker: ApplicationLifecycleTracker by inject()

  override fun onCreate() {
    super.onCreate()
    ProcessLifecycleOwner.get().lifecycle.addObserver(applicationLifecycleTracker)
    Theme
      .fromSettings(this)
      ?.apply()

    whatsNewRepository.removeNewsForNewUser()

    if (isDebug()) {
      Timber.plant(Timber.DebugTree())
    } else {
      Timber.plant(FirebaseCrashlyticsLogExceptionTree())
    }

    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

    setupDatadog()
  }

  private fun setupDatadog() {
    val configuration = Configuration.Builder(
      logsEnabled = true,
      tracesEnabled = true,
      crashReportsEnabled = true,
      rumEnabled = true,
    )
      .trackInteractions()
      .build()

    val credentials = Credentials(
      clientToken = "pub185bcba7ed324e83d068b80e25a81359",
      envName = if (BuildConfig.BUILD_TYPE == "debug" || BuildConfig.BUILD_TYPE == "staging") "dev" else "prod",
      variant = "",
      rumApplicationId = "4d7b8355-396d-406e-b543-30a073050e8f",
    )
    Datadog.initialize(this, credentials, configuration, TrackingConsent.GRANTED)

    val monitor = RumMonitor.Builder().build()
    GlobalRum.registerIfAbsent(monitor)
  }

  open val graphqlUrl get() = getString(R.string.GRAPHQL_URL)
  open val graphqlSubscriptionUrl get() = getString(R.string.WS_GRAPHQL_URL)
  open val isTestBuild = false
}
