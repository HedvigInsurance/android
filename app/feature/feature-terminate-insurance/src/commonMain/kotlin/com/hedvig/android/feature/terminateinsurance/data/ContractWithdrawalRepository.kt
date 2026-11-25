package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.Serializable
import octopus.ContractWithdrawalQuery
import octopus.WithdrawContractMutation

internal interface ContractWithdrawalRepository {
  suspend fun getContractWithdrawal(contractId: String): Either<ErrorMessage, ContractWithdrawalData>

  suspend fun withdrawContract(contractId: String): Either<ErrorMessage, WithdrawalResult>
}

@Serializable
internal data class ContractWithdrawalData(
  val statusText: String,
  val action: TerminationAction,
)

internal sealed interface WithdrawalResult {
  data object Withdrawn : WithdrawalResult

  data class UserError(val message: String) : WithdrawalResult
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class ContractWithdrawalRepositoryImpl(
  private val apolloClient: ApolloClient,
) : ContractWithdrawalRepository {
  override suspend fun getContractWithdrawal(contractId: String): Either<ErrorMessage, ContractWithdrawalData> {
    return either {
      val withdrawal = apolloClient
        .query(ContractWithdrawalQuery(contractId = contractId))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute(::ErrorMessage)
        .bind()
        .currentMember
        .contractWithdrawal
      val action = ensureNotNull(withdrawal.action.toTerminationAction()) {
        ErrorMessage("Unknown withdrawal action type: ${withdrawal.action::class.simpleName}")
      }
      ContractWithdrawalData(statusText = withdrawal.statusText, action = action)
    }
  }

  override suspend fun withdrawContract(contractId: String): Either<ErrorMessage, WithdrawalResult> {
    return either {
      val result = apolloClient
        .mutation(WithdrawContractMutation(contractId = contractId))
        .safeExecute(::ErrorMessage)
        .bind()
        .withdrawContract
      val userError = result.userError?.message
      if (userError != null) {
        WithdrawalResult.UserError(userError)
      } else {
        WithdrawalResult.Withdrawn
      }
    }
  }
}

private fun ContractWithdrawalQuery.Data.CurrentMember.ContractWithdrawal.Action.toTerminationAction():
  TerminationAction? {
  return when (this) {
    is ContractWithdrawalQuery.Data.CurrentMember.ContractWithdrawal.TerminationFlowActionTerminateWithDateAction -> {
      TerminationAction.TerminateWithDate(
        minDate = minDate,
        maxDate = maxDate,
        extraCoverageItems = extraCoverage.map { ExtraCoverageItem(it.displayName, it.displayValue) },
      )
    }

    is ContractWithdrawalQuery.Data.CurrentMember.ContractWithdrawal.TerminationFlowActionDeleteInsuranceAction -> {
      TerminationAction.DeleteInsurance(
        extraCoverageItems = extraCoverage.map { ExtraCoverageItem(it.displayName, it.displayValue) },
      )
    }

    else -> {
      null
    }
  }
}
