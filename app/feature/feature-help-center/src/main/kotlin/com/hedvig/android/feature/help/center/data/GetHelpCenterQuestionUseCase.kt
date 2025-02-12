package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage

internal interface GetHelpCenterQuestionUseCase {
  suspend fun invoke(questionId: String): Either<ErrorMessage, FAQItem>
}

internal class GetHelpCenterQuestionUseCaseImpl(
  val getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
) : GetHelpCenterQuestionUseCase {
  override suspend fun invoke(questionId: String): Either<ErrorMessage, FAQItem> {
    return either {
      val result = getHelpCenterFAQUseCase.invoke()
        .getOrNull()?.topics?.flatMap { it.commonFAQ + it.otherFAQ }?.firstOrNull { it.id == questionId }
      if (result == null) {
        raise(ErrorMessage())
      } else {
        result
      }
    }
  }
}
