package com.hedvig.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.hanalytics.android.tracking.ApplicationLifecycleTracker
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.tracking.ActivityChangeTracker
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import io.customer.messagingpush.ModuleMessagingPushFCM
import io.customer.sdk.CustomerIO
import io.customer.sdk.data.model.Region
import org.koin.android.ext.android.inject

open class HedvigApplication : Application() {
  protected val apolloClient: ApolloClient by inject()
  private val whatsNewRepository: WhatsNewRepository by inject()
  private val applicationLifecycleTracker: ApplicationLifecycleTracker by inject()

  override fun onCreate() {
    super.onCreate()
    ProcessLifecycleOwner.get().lifecycle.addObserver(applicationLifecycleTracker)
    Theme.fromSettings(this)?.apply()

    whatsNewRepository.removeNewsForNewUser()

    setupCustomerIo()

    registerActivityLifecycleCallbacks(ActivityChangeTracker())

    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
  }

  private fun setupCustomerIo() {
    CustomerIO.Builder(
      siteId = "id",
      apiKey = "key",
      appContext = this,
    ).apply {
      addCustomerIOModule(
        ModuleMessagingPushFCM(),
      )
      setRequestTimeout(8000L)
      setRegion(Region.US)
      build()
    }
  }

  open val graphqlUrl get() = getString(R.string.GRAPHQL_URL)
  open val graphqlSubscriptionUrl get() = getString(R.string.WS_GRAPHQL_URL)
}
