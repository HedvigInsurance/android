package com.hedvig.app.authenticate

import android.content.SharedPreferences
import arrow.core.identity
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.util.apollo.safeQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.yield

interface LoginStatusService {
    var isViewingOffer: Boolean
    var isLoggedIn: Boolean
    suspend fun getLoginStatus(): LoginStatus
    fun getLoginStatusAsFlow(): Flow<LoginStatus>
}

class SharedPreferencesLoginStatusService(
    private val apolloClient: ApolloClient,
    private val sharedPreferences: SharedPreferences,
    private val authenticationTokenManager: AuthenticationTokenService,
) : LoginStatusService {

    override var isViewingOffer: Boolean
        set(value) = sharedPreferences.edit()
            .putBoolean(IS_VIEWING_OFFER, value)
            .apply()
        get() = sharedPreferences.getBoolean(IS_VIEWING_OFFER, false)

    override var isLoggedIn: Boolean
        set(value) = sharedPreferences.edit()
            .putBoolean(SHARED_PREFERENCE_IS_LOGGED_IN, value)
            .apply()
        get() = sharedPreferences.getBoolean(SHARED_PREFERENCE_IS_LOGGED_IN, false)

    override suspend fun getLoginStatus(): LoginStatus {
        yield()
        return when {
            isLoggedIn -> LoginStatus.LOGGED_IN
            isViewingOffer -> LoginStatus.IN_OFFER
            authenticationTokenManager.authenticationToken == null -> LoginStatus.ONBOARDING
            hasNoContracts() -> LoginStatus.ONBOARDING
            else -> {
                isLoggedIn = true
                LoginStatus.LOGGED_IN
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getLoginStatusAsFlow(): Flow<LoginStatus> {
        return channelFlow {
            // Trigger the first emission manually since the sharedPreferences callback doesn't trigger on registration
            trySend(Unit)
            val callback: (sharedPreferences: SharedPreferences, key: String) -> Unit = { _, _ ->
                trySend(Unit)
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(callback)
            awaitClose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(callback)
            }
        }
            .mapLatest { getLoginStatus() }
            .conflate()
            .flowOn(Dispatchers.IO)
    }

    private suspend fun hasNoContracts(): Boolean {
        return apolloClient
            .query(ContractStatusQuery())
            .safeQuery()
            .toOption()
            .map { contractStatusQueryData ->
                contractStatusQueryData.contracts.isEmpty()
            }
            .fold({ true }, ::identity)
    }

    companion object {
        private const val SHARED_PREFERENCE_IS_LOGGED_IN = "shared_preference_is_logged_in"
        private const val IS_VIEWING_OFFER = "IS_VIEWING_OFFER"
    }
}
