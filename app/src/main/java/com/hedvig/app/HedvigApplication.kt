package com.hedvig.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.NewSessionMutation
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.util.extensions.SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.storeBoolean
import com.jakewharton.threetenabp.AndroidThreeTen
import e
import i
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import net.ypresto.timbertreeutils.CrashlyticsLogExceptionTree
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class HedvigApplication : Application() {
    val apolloClientWrapper: ApolloClientWrapper by inject()
    private val whatsNewRepository: WhatsNewRepository by inject()

    private val disposables = CompositeDisposable()

    override fun attachBaseContext(base: Context?) {
        Language.DefaultLocale.initialize()
        super.attachBaseContext(Language.fromSettings(base)?.apply(base))
    }

    override fun onCreate() {
        super.onCreate()

        Language
            .fromSettings(this)
            ?.apply(this)

        Theme
            .fromSettings(this)
            ?.apply()

        AndroidThreeTen.init(this)

        startKoin {
            androidLogger()
            androidContext(this@HedvigApplication)
            modules(
                listOf(
                    applicationModule,
                    viewModelModule,
                    loggedInModule,
                    dashboardModule,
                    marketingModule,
                    offerModule,
                    profileModule,
                    keyGearModule,
                    languageAndMarketModule,
                    adyenModule,
                    serviceModule,
                    repositoriesModule,
                    trackerModule
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
            acquireHedvigToken()
        }

        if (isDebug()) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsLogExceptionTree())
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private fun acquireHedvigToken() {
        disposables += Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(NewSessionMutation()))
            .subscribe({ response ->
                if (response.hasErrors()) {
                    e { "Failed to register a hedvig token: ${response.errors()}" }
                    return@subscribe
                }
                response.data()?.createSessionV2?.token?.let { hedvigToken ->
                    setAuthenticationToken(hedvigToken)
                    apolloClientWrapper.invalidateApolloClient()
                    i { "Successfully saved hedvig token" }
                } ?: e { "createSession returned no token" }
            }, { e(it) })
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
}
