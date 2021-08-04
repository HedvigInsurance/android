package com.hedvig.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.NewSessionMutation
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.util.FirebaseCrashlyticsLogExceptionTree
import com.hedvig.app.util.extensions.SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.storeBoolean
import e
import i
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
    private val marketManager: MarketManager by inject()
    private val authenticationTokenService: AuthenticationTokenService by inject()

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
                    embarkModule,
                    previousInsViewModel,
                    marketPickerTrackerModule,
                    whatsNewModule,
                    marketManagerModule,
                    connectPaymentModule,
                    trustlyModule,
                    notificationModule,
                    marketPickerModule,
                    textActionSetModule,
                    numberActionSetModule,
                    choosePlanModule,
                    clockModule,
                    embarkTrackerModule,
                    localeManagerModule,
                    changeAddressModule,
                    changeDateBottomSheetModule,
                    useCaseModule,
                    valueStoreModule,
                    onboardingModule,
                    pushTokenManagerModule,
                    checkoutModule,
                    cacheManagerModule,
                    sharedPreferencesModule,
                    featureRuntimeBehaviorModule
                )
            )
        }

        val previousLanguage = PreferenceManager
            .getDefaultSharedPreferences(this)
            .getString(SettingsActivity.SETTING_LANGUAGE, null)
        if (previousLanguage == SettingsActivity.SYSTEM_DEFAULT) {
            val market = marketManager.market
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            market?.let {
                sharedPreferences.edit()
                    .putString(
                        SettingsActivity.SETTING_LANGUAGE,
                        Language.getAvailableLanguages(market).first().toString()
                    ).commit()
            }
        }

        Language.fromSettings(this, marketManager.market).apply(this)

        if (authenticationTokenService.authenticationToken == null && !getStoredBoolean(
                SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN
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
    }

    private suspend fun acquireHedvigToken() {
        val response = runCatching {
            apolloClient.mutate(NewSessionMutation()).await()
        }
        if (response.isFailure) {
            response.exceptionOrNull()?.let { e { "Failed to register a hedvig token: $it" } }
            return
        }
        response.getOrNull()?.data?.createSessionV2?.token?.let { hedvigToken ->
            authenticationTokenService.authenticationToken = hedvigToken
            apolloClient.subscriptionManager.reconnect()
            i { "Successfully saved hedvig token" }
        } ?: e { "createSession returned no token" }
    }

    private fun tryToMigrateTokenFromReactDB() {
        val instance = LegacyReactDatabaseSupplier.getInstance(this)
        instance.getTokenIfExists()?.let { token ->
            authenticationTokenService.authenticationToken = token
            apolloClient.subscriptionManager.reconnect()
        }
        instance.clearAndCloseDatabase()
        // Let's only try this once
        storeBoolean(SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN, true)
    }

    open val graphqlUrl get() = getString(R.string.GRAPHQL_URL)
    open val graphqlSubscriptionUrl get() = getString(R.string.WS_GRAPHQL_URL)
    open val isTestBuild = false
}
