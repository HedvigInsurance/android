package com.hedvig.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import com.apollographql.apollo3.ApolloClient
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.log.Logger
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumMonitor
import com.datadog.android.timber.DatadogTree
import com.hedvig.android.core.common.preferences.PreferenceKey
import com.hedvig.android.hanalytics.tracking.ApplicationLifecycleTracker
import com.hedvig.android.market.Language
import com.hedvig.android.market.MarketManager
import com.hedvig.android.owldroid.graphql.NewSessionMutation
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.util.FirebaseCrashlyticsLogExceptionTree
import com.hedvig.app.util.apollo.reconnectSubscriptions
import com.hedvig.app.util.extensions.SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.storeBoolean
import e
import i
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

open class HedvigApplication : Application() {
  protected val apolloClient: ApolloClient by inject()
  private val whatsNewRepository: WhatsNewRepository by inject()
  private val marketManager: MarketManager by inject()
  private val authenticationTokenService: AuthenticationTokenService by inject()
  private val applicationLifecycleTracker: ApplicationLifecycleTracker by inject()

  override fun onCreate() {
    super.onCreate()
    ProcessLifecycleOwner.get().lifecycle.addObserver(applicationLifecycleTracker)
    Theme
      .fromSettings(this)
      ?.apply()

    val previousLanguage = PreferenceManager
      .getDefaultSharedPreferences(this)
      .getString(PreferenceKey.SETTING_LANGUAGE, null)
    if (previousLanguage == SettingsActivity.SYSTEM_DEFAULT) {
      val market = marketManager.market
      val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
      market?.let {
        sharedPreferences.edit()
          .putString(
            PreferenceKey.SETTING_LANGUAGE,
            Language.getAvailableLanguages(market).first().toString(),
          ).commit()
      }
    }

    Language.fromSettings(this, marketManager.market).apply(this)

    if (authenticationTokenService.authenticationToken == null && !getStoredBoolean(
        SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN,
      )
    ) {
      tryToMigrateTokenFromReactDB()
    }

    if (authenticationTokenService.authenticationToken == null) {
      whatsNewRepository.removeNewsForNewUser()
      CoroutineScope(IO).launch {
        acquireHedvigToken()
      }
    }

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
      clientToken = BuildConfig.DATADOG_CLIENT_TOKEN,
      envName = if (BuildConfig.BUILD_TYPE == "debug" || BuildConfig.BUILD_TYPE == "staging") "dev" else "prod",
      variant = "",
      rumApplicationId = BuildConfig.DATADOG_APPLICATION_ID,
    )
    // TODO Ask for permission to track and set trackingConsent accordingly
    Datadog.initialize(this, credentials, configuration, TrackingConsent.GRANTED)

    val logger = Logger.Builder()
      .setNetworkInfoEnabled(true)
      .setLogcatLogsEnabled(true)
      .setDatadogLogsEnabled(true)
      .setBundleWithTraceEnabled(true)
      .build()

    Timber.plant(DatadogTree(logger))

    val monitor = RumMonitor.Builder().build()
    GlobalRum.registerIfAbsent(monitor)
  }

  private suspend fun acquireHedvigToken() {
    val response = runCatching {
      apolloClient.mutation(NewSessionMutation()).execute()
    }
    if (response.isFailure) {
      response.exceptionOrNull()?.let { e { "Failed to register a hedvig token: $it" } }
      return
    }
    response.getOrNull()?.data?.createSessionV2?.token?.let { hedvigToken ->
      authenticationTokenService.authenticationToken = hedvigToken
      apolloClient.reconnectSubscriptions()
      i { "Successfully saved hedvig token" }
    } ?: e { "createSession returned no token" }
  }

  private fun tryToMigrateTokenFromReactDB() {
    val instance = LegacyReactDatabaseSupplier.getInstance(this)
    instance.getTokenIfExists()?.let { token ->
      authenticationTokenService.authenticationToken = token
      apolloClient.reconnectSubscriptions()
    }
    instance.clearAndCloseDatabase()
    // Let's only try this once
    storeBoolean(SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN, true)
  }

  open val graphqlUrl get() = getString(R.string.GRAPHQL_URL)
  open val graphqlSubscriptionUrl get() = getString(R.string.WS_GRAPHQL_URL)
  open val isTestBuild = false
}
