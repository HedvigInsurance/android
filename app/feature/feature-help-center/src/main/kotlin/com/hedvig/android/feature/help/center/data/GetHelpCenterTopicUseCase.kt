package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage

internal interface GetHelpCenterTopicUseCase {
  suspend fun invoke(topicId: String): Either<ErrorMessage, FAQTopic>
}

internal class GetHelpCenterTopicUseCaseImpl(
  private val getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
) : GetHelpCenterTopicUseCase {
  override suspend fun invoke(topicId: String): Either<ErrorMessage, FAQTopic> {
    return either {
      val result = getHelpCenterFAQUseCase.invoke()
        .getOrNull()?.topics?.firstOrNull { it.id == topicId }
      if (result == null) {
        raise(ErrorMessage())
      } else {
        result
      }
    }
  }

}

