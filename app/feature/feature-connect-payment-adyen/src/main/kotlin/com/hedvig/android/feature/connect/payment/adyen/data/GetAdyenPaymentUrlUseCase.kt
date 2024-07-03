package com.hedvig.android.feature.connect.payment.adyen.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService

internal class GetAdyenPaymentUrlUseCase(
//  private val paymentRepository: PaymentRepository,
  private val languageService: LanguageService,
) {
  suspend fun invoke(): Either<ErrorMessage, AdyenPaymentUrl> {
    return either {
//      val response = paymentRepository.getMemberAuthorizationCode(languageService.getLanguage().webPath())
//      val memberPaymentUrl: MemberPaymentUrl = when (response) {
//        is MemberAuthorizationCodeResult.Error -> raise(ErrorMessage(response.error.message))
//        is MemberAuthorizationCodeResult.Success -> response.memberPaymentUrl
//      }
//      AdyenPaymentUrl(memberPaymentUrl.url)
      TODO("todo does not work without authlib")
    }
  }
}

@JvmInline
internal value class AdyenPaymentUrl(
  val url: String,
)
