package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage

internal interface GetHelpCenterTopicUseCase {
  suspend fun invoke(topicId: String): Either<ErrorMessage, FAQTopic>
}

internal class GetHelpCenterTopicUseCaseImpl(
  val getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase
): GetHelpCenterTopicUseCase {
  override suspend fun invoke(topicId: String): Either<ErrorMessage, FAQTopic> {
    TODO()
  }
}

