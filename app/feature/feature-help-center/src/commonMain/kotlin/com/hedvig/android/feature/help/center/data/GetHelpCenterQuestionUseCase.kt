package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage

internal interface GetHelpCenterQuestionUseCase {
  suspend fun invoke(questionId: String): Either<HelpCenterQuestionError, FAQItem>
}

internal class GetHelpCenterQuestionUseCaseImpl(
  val getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
) : GetHelpCenterQuestionUseCase {
  override suspend fun invoke(questionId: String): Either<HelpCenterQuestionError, FAQItem> {
    return either {
      val memberFaq = getHelpCenterFAQUseCase.invoke()
        .mapLeft { HelpCenterQuestionError.GenericError(it) }
        .bind()
      val question = memberFaq
        .topics
        .flatMap { it.commonFAQ + it.otherFAQ }
        .firstOrNull { it.id == questionId }
      if (question == null) {
        raise(HelpCenterQuestionError.NoQuestionFound)
      } else {
        question
      }
    }
  }
}

internal sealed interface HelpCenterQuestionError {
  object NoQuestionFound : HelpCenterQuestionError

  data class GenericError(val errorMessage: ErrorMessage) : HelpCenterQuestionError, ErrorMessage by errorMessage
}
