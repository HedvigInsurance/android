package com.hedvig.android.feature.insurances.insurancedetail

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.insurancedetail.GetContractForContractIdUseCaseImpl.GetContractForContractIdError
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal interface GetContractForContractIdUseCase {
  fun invoke(contractId: String): Flow<Either<GetContractForContractIdError, InsuranceContract>>
}

internal class GetContractForContractIdUseCaseImpl(
  private val getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
) : GetContractForContractIdUseCase {
  override fun invoke(contractId: String): Flow<Either<GetContractForContractIdError, InsuranceContract>> {
    return flow {
      getInsuranceContractsUseCaseProvider
        .provide()
        .invoke(forceNetworkFetch = false)
        .map { insuranceContractResult ->
          either {
            val contract = insuranceContractResult
              .mapLeft { GetContractForContractIdError.GenericError(it) }
              .bind()
              .firstOrNull { it.id == contractId }
            ensureNotNull(contract) {
              GetContractForContractIdError.NoContractFound(
                ErrorMessage("No contract found with id: $contractId").also {
                  logcat(LogPriority.ERROR) { it.message.toString() }
                },
              )
            }
          }
        }
    }
  }

  internal sealed interface GetContractForContractIdError {
    data class NoContractFound(
      val errorMessage: ErrorMessage,
    ) : GetContractForContractIdError, ErrorMessage by errorMessage

    data class GenericError(val errorMessage: ErrorMessage) :
      GetContractForContractIdError,
      ErrorMessage by errorMessage
  }
}
