package com.hedvig.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.hanalytics.android.tracking.ApplicationLifecycleTracker
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.tracking.ActivityChangeTracker
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.util.firebase.FirebaseBreadcrumbTimberTree
import com.hedvig.app.util.firebase.FirebaseCrashlyticsLogExceptionTree
import org.koin.android.ext.android.inject
import timber.log.Timber

open class HedvigApplication : Application() {
  protected val apolloClient: ApolloClient by inject()
  private val whatsNewRepository: WhatsNewRepository by inject()
  private val applicationLifecycleTracker: ApplicationLifecycleTracker by inject()

  override fun onCreate() {
    super.onCreate()
    ProcessLifecycleOwner.get().lifecycle.addObserver(applicationLifecycleTracker)
    Theme.fromSettings(this)?.apply()

    whatsNewRepository.removeNewsForNewUser()

    if (isDebug()) {
      Timber.plant(Timber.DebugTree())
    }
    Timber.plant(FirebaseBreadcrumbTimberTree())
    Timber.plant(FirebaseCrashlyticsLogExceptionTree())

    registerActivityLifecycleCallbacks(ActivityChangeTracker())

    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
  }

  open val graphqlUrl get() = getString(R.string.GRAPHQL_URL)
  open val graphqlSubscriptionUrl get() = getString(R.string.WS_GRAPHQL_URL)
}
