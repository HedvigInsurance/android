package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage

internal interface GetHelpCenterTopicUseCase {
  suspend fun invoke(topicId: String): Either<HelpCenterTopicError, FAQTopic>
}

internal class GetHelpCenterTopicUseCaseImpl(
  private val getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
) : GetHelpCenterTopicUseCase {
  override suspend fun invoke(topicId: String): Either<HelpCenterTopicError, FAQTopic> {
    return either {
      val memberFaqs = getHelpCenterFAQUseCase.invoke()
        .mapLeft { HelpCenterTopicError.GenericError(it) }
        .bind()
      val topic = memberFaqs.topics.firstOrNull { it.id == topicId }
      if (topic == null) {
        raise(HelpCenterTopicError.NoTopicFound)
      } else {
        topic
      }
    }
  }
}

internal sealed interface HelpCenterTopicError {
  object NoTopicFound : HelpCenterTopicError

  data class GenericError(val errorMessage: ErrorMessage) : HelpCenterTopicError, ErrorMessage by errorMessage
}
