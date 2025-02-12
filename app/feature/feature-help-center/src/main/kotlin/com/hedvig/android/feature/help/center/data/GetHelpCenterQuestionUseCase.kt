package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage

internal interface GetHelpCenterQuestionUseCase {
  suspend fun invoke(questionId: String) : Either<ErrorMessage, FAQItem>
}

internal class GetHelpCenterQuestionUseCaseImpl(
  val getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase
): GetHelpCenterQuestionUseCase {
  override suspend fun invoke(questionId: String): Either<ErrorMessage, FAQItem> {
    TODO("Not yet implemented")
  }

}
