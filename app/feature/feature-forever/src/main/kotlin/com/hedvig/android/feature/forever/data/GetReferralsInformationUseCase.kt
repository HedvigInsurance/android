package com.hedvig.android.feature.forever.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import giraffe.ReferralTermsQuery

interface GetReferralsInformationUseCase {
  suspend fun invoke(): Either<ErrorMessage, ReferralTermsQuery.ReferralTerms>
}

internal class GetReferralsInformationUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) : GetReferralsInformationUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ReferralTermsQuery.ReferralTerms> {
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
