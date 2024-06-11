package com.hedvig.android.feature.editcoinsured.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import octopus.CoInsuredQuery

internal interface GetCoInsuredUseCase {
  suspend fun invoke(contractId: String): Either<CoInsuredError, CoInsuredResult>
}

internal class GetCoInsuredUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCoInsuredUseCase {
  override suspend fun invoke(contractId: String): Either<CoInsuredError, CoInsuredResult> = either {
    val result = apolloClient.query(CoInsuredQuery())
      .safeExecute()
      .toEither(::ErrorMessage)
      .mapLeft { CoInsuredError.GenericError(it.message) }
      .bind()

    val contract = result.currentMember.activeContracts.firstOrNull { it.id == contractId }

    ensureNotNull(contract) {
      CoInsuredError.ContractNotFound
    }

    val coInsuredOnContract = contract.let {
      it.coInsured
        ?.filter { it.terminatesOn == null }
        ?.map { it.toCoInsured() }
        ?: listOf()
    }

    val allCoInsured = result.currentMember.activeContracts
      .mapNotNull { it.coInsured?.map { it.toCoInsured() } }
      .flatten()
      .filterNot { coinsured -> coinsured.hasMissingInfo || coInsuredOnContract.any { it.id == coinsured.id } }

    CoInsuredResult(
      member = Member(
        firstName = result.currentMember.firstName,
        lastName = result.currentMember.lastName,
        ssn = result.currentMember.ssn,
      ),
      coInsuredOnContract = coInsuredOnContract,
      allCoInsured = allCoInsured,
    )
  }

  private fun CoInsuredQuery.Data.CurrentMember.ActiveContract.CoInsured.toCoInsured() = CoInsured(
    firstName = firstName,
    lastName = lastName,
    birthDate = birthdate,
    ssn = ssn,
    hasMissingInfo = hasMissingInfo,
  )
}

internal sealed interface CoInsuredError {
  data class GenericError(val message: String?) : CoInsuredError

  data object ContractNotFound : CoInsuredError
}

internal data class CoInsuredResult(
  val member: Member,
  val coInsuredOnContract: List<CoInsured>,
  val allCoInsured: List<CoInsured>,
)
