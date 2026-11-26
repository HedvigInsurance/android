package com.hedvig.android.feature.terminateinsurance

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.data.ContractWithdrawalData
import com.hedvig.android.feature.terminateinsurance.data.ContractWithdrawalRepository
import com.hedvig.android.feature.terminateinsurance.data.WithdrawalResult

internal class FakeContractWithdrawalRepository(
  private val withdrawalData: Either<ErrorMessage, ContractWithdrawalData> = Either.Left(ErrorMessage()),
  private val withdrawResult: Either<ErrorMessage, WithdrawalResult> = Either.Left(ErrorMessage()),
) : ContractWithdrawalRepository {
  val withdrawalRequests = mutableListOf<String>()
  val withdrawRequests = mutableListOf<String>()

  override suspend fun getContractWithdrawal(contractId: String): Either<ErrorMessage, ContractWithdrawalData> {
    withdrawalRequests.add(contractId)
    return withdrawalData
  }

  override suspend fun withdrawContract(contractId: String): Either<ErrorMessage, WithdrawalResult> {
    withdrawRequests.add(contractId)
    return withdrawResult
  }
}
