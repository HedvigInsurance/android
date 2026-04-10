package com.hedvig.android.feature.payoutaccount.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage

internal interface UpdateBankAccountUseCase {
  suspend fun invoke(clearingNumber: String, accountNumber: String): Either<ErrorMessage, Unit>
}

internal class UpdateBankAccountUseCaseImpl(
  private val getPayoutAccountUseCase: GetPayoutAccountUseCaseImpl,
) : UpdateBankAccountUseCase {
  override suspend fun invoke(clearingNumber: String, accountNumber: String): Either<ErrorMessage, Unit> {
    val bankName = bankNameForClearingNumber(clearingNumber)
    getPayoutAccountUseCase.update(
      PayoutAccount.BankAccount(
        clearingNumber = clearingNumber,
        accountNumber = accountNumber,
        bankName = bankName,
      ),
    )
    return Unit.right()
  }
}
