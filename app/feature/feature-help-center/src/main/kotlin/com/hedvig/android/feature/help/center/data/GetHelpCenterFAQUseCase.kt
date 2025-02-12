package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage

internal interface GetHelpCenterFAQUseCase {
  suspend fun invoke(): Either<ErrorMessage, MemberFAQ>
}

data class FAQItem(
  val id: String,
  val question: String,
  val answer: String
)

data class FAQTopic(
  val id: String,
  val title: String,
  val commonFAQ: List<FAQItem>,
  val otherFAQ: List<FAQItem>,
)

data class MemberFAQ (
  val commonFAQ: List<FAQItem>,
  val topics: List<FAQTopic>
)

internal class GetHelpCenterFAQUseCaseImpl(
  apolloClient: ApolloClient
): GetHelpCenterFAQUseCase {
  override suspend fun invoke(): Either<ErrorMessage, MemberFAQ> {
    TODO("Not yet implemented")
  }
//HelpCenterFAQ
}
