package com.hedvig.android.feature.payoutaccount.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage

internal interface GetPayoutAccountUseCase {
  suspend fun invoke(): Either<ErrorMessage, PayoutAccount>
}

internal class GetPayoutAccountUseCaseImpl : GetPayoutAccountUseCase {
  private var currentAccount: PayoutAccount = PayoutAccount.BankAccount(
    clearingNumber = "3300",
    accountNumber = "9202195211",
    bankName = "Nordea",
  )

  fun update(payoutAccount: PayoutAccount) {
    currentAccount = payoutAccount
  }

  override suspend fun invoke(): Either<ErrorMessage, PayoutAccount> {
    return currentAccount.right()
  }
}
