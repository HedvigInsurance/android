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
import com.hedvig.android.apollo.graphql.NewSessionMutation
import com.hedvig.android.hanalytics.android.tracking.ApplicationLifecycleTracker
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.util.FirebaseCrashlyticsLogExceptionTree
import com.hedvig.app.util.apollo.reconnectSubscriptions
import com.hedvig.app.util.extensions.SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.storeBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import slimber.log.e
import slimber.log.i
import timber.log.Timber

open class HedvigApplication : Application() {
  protected val apolloClient: ApolloClient by inject()
  private val whatsNewRepository: WhatsNewRepository by inject()
  private val authenticationTokenService: AuthenticationTokenService by inject()
  private val applicationLifecycleTracker: ApplicationLifecycleTracker by inject()

  override fun onCreate() {
    super.onCreate()
    ProcessLifecycleOwner.get().lifecycle.addObserver(applicationLifecycleTracker)
    Theme
      .fromSettings(this)
      ?.apply()

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
      clientToken = "pub185bcba7ed324e83d068b80e25a81359",
      envName = if (BuildConfig.BUILD_TYPE == "debug" || BuildConfig.BUILD_TYPE == "staging") "dev" else "prod",
      variant = "",
      rumApplicationId = "4d7b8355-396d-406e-b543-30a073050e8f",
    )
    Datadog.initialize(this, credentials, configuration, TrackingConsent.GRANTED)

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
