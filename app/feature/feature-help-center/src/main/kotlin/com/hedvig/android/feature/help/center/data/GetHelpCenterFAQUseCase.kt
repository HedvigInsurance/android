package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.HelpCenterFAQQuery
import octopus.fragment.FAQItemFragment

internal interface GetHelpCenterFAQUseCase {
  suspend fun invoke(): Either<ErrorMessage, MemberFAQ>
}

data class FAQItem(
  val id: String,
  val question: String,
  val answer: String,
)

data class FAQTopic(
  val id: String,
  val title: String,
  val commonFAQ: List<FAQItem>,
  val otherFAQ: List<FAQItem>,
)

data class MemberFAQ(
  val commonFAQ: List<FAQItem>,
  val topics: List<FAQTopic>,
)

internal class GetHelpCenterFAQUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetHelpCenterFAQUseCase {
  override suspend fun invoke(): Either<ErrorMessage, MemberFAQ> {
    return either {
      val result = apolloClient
        .query(HelpCenterFAQQuery())
        .safeExecute(::ErrorMessage)
        .onLeft { logcat(LogPriority.ERROR) { "Could not fetch contracts ${it.message}" } }
        .bind()
        .currentMember.memberFAQ
      MemberFAQ(
          commonFAQ = result.commonFAQ.map { item ->
              item.toFAQItem()
          },
          topics = result.topics.map { topic ->
              FAQTopic(
                  id = topic.id,
                  title = topic.title,
                  commonFAQ = topic.commonFAQ.map { item ->
                      item.toFAQItem()
                  },
                  otherFAQ = topic.otherFAQ.map { item ->
                      item.toFAQItem()
                  },
              )
          },
      )
    }
  }
}


private fun FAQItemFragment.toFAQItem(): FAQItem {
  return FAQItem(
      id = id,
      question = question,
      answer = answer,
  )
}
