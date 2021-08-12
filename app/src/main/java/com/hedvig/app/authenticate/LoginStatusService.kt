package com.hedvig.app.authenticate

import android.content.SharedPreferences
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.ContractStatusQuery

interface LoginStatusService {
    var isViewingOffer: Boolean
    var isLoggedIn: Boolean
    suspend fun getLoginStatus(): LoginStatus
}

class SharedPreferencesLoginStatusService(
    private val apolloClient: ApolloClient,
    private val sharedPreferences: SharedPreferences,
    private val authenticationTokenManager: AuthenticationTokenService
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

    override suspend fun getLoginStatus() = when {
        isLoggedIn -> LoginStatus.LOGGED_IN
        isViewingOffer -> LoginStatus.IN_OFFER
        authenticationTokenManager.authenticationToken == null -> LoginStatus.ONBOARDING
        hasNoContracts() -> LoginStatus.ONBOARDING
        else -> {
            isLoggedIn = true
            LoginStatus.LOGGED_IN
        }
    }

    private suspend fun hasNoContracts(): Boolean {
        val response = runCatching {
            apolloClient.query(ContractStatusQuery()).await()
        }

        if (response.isFailure || response.getOrNull()?.data?.contracts.orEmpty().isEmpty()) {
            return true
        }
        return false
    }

    companion object {
        private const val SHARED_PREFERENCE_IS_LOGGED_IN = "shared_preference_is_logged_in"
        private const val IS_VIEWING_OFFER = "IS_VIEWING_OFFER"
    }
}
