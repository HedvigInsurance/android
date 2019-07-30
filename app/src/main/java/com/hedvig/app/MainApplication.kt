package com.hedvig.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.NewSessionMutation
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.service.TextKeys
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN
import com.ice.restring.Restring
import com.jakewharton.threetenabp.AndroidThreeTen
import io.branch.referral.Branch
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import net.ypresto.timbertreeutils.CrashlyticsLogExceptionTree
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class MainApplication : Application() {

    val apolloClientWrapper: ApolloClientWrapper by inject()
    private val whatsNewRepository: WhatsNewRepository by inject()

    private val disposables = CompositeDisposable()

    private val textKeys: TextKeys by inject()

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        if (getAuthenticationToken() == null && !getStoredBoolean(SHARED_PREFERENCE_TRIED_MIGRATION_OF_TOKEN)) {
            tryToMigrateTokenFromReactDB()
        }

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                applicationModule,
                viewModelModule,
                serviceModule,
                repositoriesModule,
                trackerModule
            )
        }

        if (getAuthenticationToken() == null) {
            whatsNewRepository.removeNewsForNewUser()
            acquireHedvigToken()
        }

        // TODO Remove this probably? Or figure out a better solve for the problem
        if (BuildConfig.DEBUG || BuildConfig.APP_ID == "com.hedvig.test.app") {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsLogExceptionTree())
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        setupRestring()

        Branch.getAutoInstance(this)
    }

    private fun acquireHedvigToken() {
        disposables += Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(NewSessionMutation()))
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e("Failed to register a hedvig token: %s", response.errors().toString())
                    return@subscribe
                }
                response.data()?.createSessionV2?.token?.let { hedvigToken ->
                    setAuthenticationToken(hedvigToken)
                    apolloClientWrapper.invalidateApolloClient()
                    Timber.i("Successfully saved hedvig token")
                } ?: Timber.e("createSession returned no token")
            }, { Timber.e(it) })
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

    private fun setupRestring() {
        val versionSharedPreferences =
            getSharedPreferences(LAST_OPENED_VERSION_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        if (versionSharedPreferences.contains(LAST_OPENED_VERSION)) {
            if (versionSharedPreferences.getInt(LAST_OPENED_VERSION, 0) != BuildConfig.VERSION_CODE) {
                getSharedPreferences("Restrings", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
            }
        } else {
            versionSharedPreferences
                .edit()
                .putInt(LAST_OPENED_VERSION, BuildConfig.VERSION_CODE)
                .apply()
        }
        Restring.init(this)
        try {
            textKeys.refreshTextKeys()
        } catch (exception: Exception) {
            Timber.e(exception)
        }
    }

    companion object {
        private const val LAST_OPENED_VERSION_SHARED_PREFERENCES = "last_opened_version_prefs"
        private const val LAST_OPENED_VERSION = "Last_opened_version"
    }
}
