package com.hedvig.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.NewSessionMutation
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.util.FirebaseCrashlyticsLogExceptionTree
import com.hedvig.app.util.extensions.SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.setAuthenticationToken
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
    val apolloClientWrapper: ApolloClientWrapper by inject()
    private val whatsNewRepository: WhatsNewRepository by inject()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(Language.fromSettings(base)?.apply(base))
    }

    override fun onCreate() {
        super.onCreate()

        val previousLanguage = PreferenceManager
            .getDefaultSharedPreferences(this)
            .getString(SettingsActivity.SETTING_LANGUAGE, null)
        if (previousLanguage == SettingsActivity.SYSTEM_DEFAULT) {
            val market = getMarket()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            market?.let {
                sharedPreferences.edit()
                    .putString(
                        SettingsActivity.SETTING_LANGUAGE,
                        Language.getAvailableLanguages(market).first().toString()
                    ).commit()
            }
        }

        Language
            .fromSettings(this)
            ?.apply(this)

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
                    trackerModule,
                    embarkModule,
                    previousInsViewModel,
                    marketPickerTrackerModule,
                    whatsNewModule,
                    marketProviderModule,
                    connectPaymentModule,
                    trustlyModule,
                    notificationModule,
                    marketPickerModule,
                    moreOptionsModule,
                    textActionSetModule,
                    numberActionModule,
                    choosePlanModule,
                )
            )
        }

        if (getAuthenticationToken() == null && !getStoredBoolean(
                SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN
            )
        ) {
            tryToMigrateTokenFromReactDB()
        }

        if (getAuthenticationToken() == null) {
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
            apolloClientWrapper.apolloClient.mutate(NewSessionMutation()).await()
        }
        if (response.isFailure) {
            response.exceptionOrNull()?.let { e { "Failed to register a hedvig token: $it" } }
            return
        }
        response.getOrNull()?.data?.createSessionV2?.token?.let { hedvigToken ->
            setAuthenticationToken(hedvigToken)
            apolloClientWrapper.invalidateApolloClient()
            i { "Successfully saved hedvig token" }
        } ?: e { "createSession returned no token" }
    }

    private fun tryToMigrateTokenFromReactDB() {
        val instance = LegacyReactDatabaseSupplier.getInstance(this)
        instance.getTokenIfExists()?.let { token ->
            setAuthenticationToken(token)
            apolloClientWrapper.invalidateApolloClient()
        }
        instance.clearAndCloseDatabase()
        // Let's only try this once
        storeBoolean(SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN, true)
    }

    open val graphqlUrl = BuildConfig.GRAPHQL_URL
    open val isTestBuild = false
}
