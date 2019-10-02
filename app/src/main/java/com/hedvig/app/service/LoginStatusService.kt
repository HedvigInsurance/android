package com.hedvig.app.service

import android.content.Context
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.InsuranceStatusQuery
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn
import io.reactivex.Observable
import timber.log.Timber

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

        return Rx2Apollo.from(apolloClientWrapper.apolloClient.query(InsuranceStatusQuery()))
            .map { response ->
                response.data()?.insurance?.status?.let { status ->
                    when (status) {
                        InsuranceStatus.ACTIVE,
                        InsuranceStatus.INACTIVE,
                        InsuranceStatus.INACTIVE_WITH_START_DATE -> {
                            context.setIsLoggedIn(true)
                            LoginStatus.LOGGED_IN
                        }
                        InsuranceStatus.TERMINATED -> {
                            LoginStatus.LOGGED_IN_TERMINATED
                        }
                        InsuranceStatus.PENDING,
                        InsuranceStatus.`$UNKNOWN` -> {
                            context.setIsLoggedIn(false)
                            LoginStatus.ONBOARDING
                        }
                    }
                } ?: LoginStatus.ONBOARDING
            }
    }

    companion object {
        const val IS_VIEWING_OFFER = "IS_VIEWING_OFFER"
    }
}
