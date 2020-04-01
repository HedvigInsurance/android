package com.hedvig.app.service

import android.content.Context
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn
import io.reactivex.Observable

class LoginStatusService(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun getLoginStatus(): Observable<LoginStatus> {
        if (context.isLoggedIn()) {
            return Observable.just(LoginStatus.LOGGED_IN)
        }

        val isViewingOffer = context.getStoredBoolean(IS_VIEWING_OFFER)
        if (isViewingOffer) {
            return Observable.just(LoginStatus.IN_OFFER)
        }

        context.getAuthenticationToken() ?: return Observable.just(LoginStatus.ONBOARDING)

        return Rx2Apollo.from(apolloClientWrapper.apolloClient.query(ContractStatusQuery()))
            .map { response ->
                if (response.data()?.contracts?.isEmpty() == true) {
                    return@map LoginStatus.ONBOARDING
                }

                if (isTerminated(response.data()?.contracts)) {
                    return@map LoginStatus.LOGGED_IN_TERMINATED
                }

                context.setIsLoggedIn(true)

                LoginStatus.LOGGED_IN
            }
    }

    companion object {

        private fun isTerminated(contracts: List<ContractStatusQuery.Contract>?) = contracts?.isNotEmpty() == true && contracts.all { it.status.__typename == "TerminatedStatus" }

        const val IS_VIEWING_OFFER = "IS_VIEWING_OFFER"
    }
}
