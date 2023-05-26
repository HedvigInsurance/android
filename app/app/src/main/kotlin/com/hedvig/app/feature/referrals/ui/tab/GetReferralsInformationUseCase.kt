package com.hedvig.app.feature.referrals.ui.tab

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import giraffe.ReferralTermsQuery

class GetReferralsInformationUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke(): Either<ErrorMessage, ReferralTermsQuery.ReferralTerms> {
    return either {
      apolloClient
        .query(ReferralTermsQuery(languageService.getGraphQLLocale()))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
        .referralTerms
    }
  }
}
