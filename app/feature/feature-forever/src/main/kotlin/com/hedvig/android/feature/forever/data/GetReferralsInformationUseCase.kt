package com.hedvig.android.feature.forever.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import giraffe.ReferralTermsQuery

interface GetReferralsInformationUseCase {
  suspend fun invoke(): Either<ErrorMessage, ReferralTermsQuery.ReferralTerms>
}
