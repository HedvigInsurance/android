package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

internal interface GetHelpCenterTopicUseCase {
  suspend fun invoke(topicId: String): Either<HelpCenterTopicError, FAQTopic>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetHelpCenterTopicUseCaseImpl(
  private val getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
) : GetHelpCenterTopicUseCase {
  override suspend fun invoke(topicId: String): Either<HelpCenterTopicError, FAQTopic> {
    return either {
      val memberFaq = getHelpCenterFAQUseCase.invoke()
        .mapLeft { HelpCenterTopicError.GenericError(it) }
        .bind()
      val topic = memberFaq.topics.firstOrNull { it.id == topicId }
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
