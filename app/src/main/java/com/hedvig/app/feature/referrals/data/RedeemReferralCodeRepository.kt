package com.hedvig.app.feature.referrals.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class RedeemReferralCodeRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend fun redeemReferralCode(
        code: String
    ): Either<ErrorMessage, RedeemReferralCodeMutation.Data?> {
        return apolloClient
            .mutate(RedeemReferralCodeMutation(code, localeManager.defaultLocale()))
            .safeQuery()
            .toEither()
            .mapLeft { ErrorMessage(it.message) }
    }
}
