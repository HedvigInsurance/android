package com.hedvig.android.feature.forever.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import giraffe.ReferralTermsQuery

class GetReferralsInformationDemoUseCaseImpl : GetReferralsInformationUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ReferralTermsQuery.ReferralTerms> = either {
    ReferralTermsQuery.ReferralTerms("test")
  }
}
