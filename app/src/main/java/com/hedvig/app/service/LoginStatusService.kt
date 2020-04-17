package com.hedvig.app.service

import android.content.Context
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toDeferred
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn

class LoginStatusService(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
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

        val response =
            apolloClientWrapper.apolloClient.query(ContractStatusQuery()).toDeferred().await()

        if (response.data()?.contracts?.isEmpty() == true) {
            return LoginStatus.ONBOARDING
        }

        if (isTerminated(response.data()?.contracts)) {
            return LoginStatus.LOGGED_IN_TERMINATED
        }

        context.setIsLoggedIn(true)

        return LoginStatus.LOGGED_IN
    }

    companion object {

        private fun isTerminated(contracts: List<ContractStatusQuery.Contract>?) =
            contracts?.isNotEmpty() == true && contracts.all { it.status.__typename == "TerminatedStatus" }

        const val IS_VIEWING_OFFER = "IS_VIEWING_OFFER"
    }
}
