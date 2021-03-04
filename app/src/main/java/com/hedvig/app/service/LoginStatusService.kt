package com.hedvig.app.service

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn

class LoginStatusService(
    private val apolloClient: ApolloClient,
    private val context: Context,
) {
    suspend fun getLoginStatus(): LoginStatus {
        if (context.isLoggedIn()) {
            return LoginStatus.LOGGED_IN
        }

        val isViewingOffer = context.getStoredBoolean(IS_VIEWING_OFFER)
        if (isViewingOffer) {
            return LoginStatus.IN_OFFER
        }

        context.getAuthenticationToken() ?: return LoginStatus.ONBOARDING

        val response = runCatching {
            apolloClient.query(ContractStatusQuery()).await()
        }

        if (response.isFailure || response.getOrNull()?.data?.contracts.orEmpty().isEmpty()) {
            return LoginStatus.ONBOARDING
        }

        context.setIsLoggedIn(true)

        return LoginStatus.LOGGED_IN
    }

    companion object {
        const val IS_VIEWING_OFFER = "IS_VIEWING_OFFER"
    }
}
