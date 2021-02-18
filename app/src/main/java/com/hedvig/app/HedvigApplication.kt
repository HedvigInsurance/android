package com.hedvig.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.apollographql.apollo.ApolloClient
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.util.FirebaseCrashlyticsLogExceptionTree
import com.hedvig.app.util.apollo.AuthenticationTokenHandler
import com.hedvig.app.util.extensions.SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.storeBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

open class HedvigApplication : Application() {
    protected val apolloClient: ApolloClient by inject()
    private val whatsNewRepository: WhatsNewRepository by inject()
    private val sessionTokenRequestHandler: AuthenticationTokenHandler by inject()

    override fun onCreate() {
        super.onCreate()

        Theme
            .fromSettings(this)
            ?.apply()

        startKoin {
            androidLogger()
            androidContext(this@HedvigApplication)
            modules(
                listOf(
                    applicationModule,
                    viewModelModule,
                    loggedInModule,
                    insuranceModule,
                    marketingModule,
                    offerModule,
                    profileModule,
                    paymentModule,
                    keyGearModule,
                    adyenModule,
                    referralsModule,
                    homeModule,
                    serviceModule,
                    repositoriesModule,
                    localeBroadcastManagerModule,
                    trackerModule,
                    marketPickerTrackerModule,
                    whatsNewModule,
                    marketManagerModule,
                    connectPaymentModule,
                    trustlyModule,
                    notificationModule,
                    marketPickerModule,
                    clockModule,
                    defaultLocaleModule,
                    sessionTokenModule
                )
            )
        }

        if (!sessionTokenRequestHandler.hasAuthenticationToken() && !getStoredBoolean(SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN)) {
            tryToMigrateTokenFromReactDB()
        }

        if (sessionTokenRequestHandler.hasAuthenticationToken()) {
            whatsNewRepository.removeNewsForNewUser()
            CoroutineScope(IO).launch {
                sessionTokenRequestHandler.acquireAuthenticationToken()
            }
        }

        if (isDebug()) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(FirebaseCrashlyticsLogExceptionTree())
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private fun tryToMigrateTokenFromReactDB() {
        val instance = LegacyReactDatabaseSupplier.getInstance(this)
        instance.getTokenIfExists()?.let { token ->
            setAuthenticationToken(token)
            apolloClient.subscriptionManager.reconnect()
        }
        instance.clearAndCloseDatabase()
        // Let's only try this once
        storeBoolean(SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN, true)
    }

    open val graphqlUrl = BuildConfig.GRAPHQL_URL
    open val isTestBuild = false
}
