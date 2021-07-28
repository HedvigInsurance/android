package com.hedvig.app.service

import android.content.Context
import android.content.SharedPreferences
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.util.extensions.getAuthenticationToken

private const val SHARED_PREFERENCE_IS_LOGGED_IN = "shared_preference_is_logged_in"
private const val IS_VIEWING_OFFER = "IS_VIEWING_OFFER"

class LoginStatusService(
    private val apolloClient: ApolloClient,
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    var isViewingOffer: Boolean
        set(value) = sharedPreferences.edit()
            .putBoolean(IS_VIEWING_OFFER, value)
            .apply()
        get() = sharedPreferences.getBoolean(IS_VIEWING_OFFER, false)

    var isLoggedIn: Boolean
        set(value) = sharedPreferences.edit()
            .putBoolean(SHARED_PREFERENCE_IS_LOGGED_IN, value)
            .apply()
        get() = sharedPreferences.getBoolean(SHARED_PREFERENCE_IS_LOGGED_IN, false)

    suspend fun getLoginStatus() = when {
        isLoggedIn -> LoginStatus.LOGGED_IN
        isViewingOffer -> LoginStatus.IN_OFFER
        context.getAuthenticationToken() == null -> LoginStatus.ONBOARDING
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
}
