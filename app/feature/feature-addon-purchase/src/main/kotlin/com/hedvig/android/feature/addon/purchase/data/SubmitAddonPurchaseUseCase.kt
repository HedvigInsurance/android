package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.LocalDate

internal interface SubmitAddonPurchaseUseCase {
  suspend fun invoke(quoteId: String, addonId: String): Either<ErrorMessage, LocalDate>
}
