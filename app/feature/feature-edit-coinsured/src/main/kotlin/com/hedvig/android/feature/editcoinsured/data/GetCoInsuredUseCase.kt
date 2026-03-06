package com.hedvig.android.feature.editcoinsured.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import octopus.CoInsuredQuery
import octopus.fragment.CoInsuredFragment

internal interface GetCoInsuredUseCase {
  suspend fun invoke(contractId: String): Either<CoInsuredError, CoInsuredResult>
}

internal class GetCoInsuredUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCoInsuredUseCase {
  override suspend fun invoke(contractId: String): Either<CoInsuredError, CoInsuredResult> = either {
    val result = apolloClient.query(CoInsuredQuery())
      .safeExecute(::ErrorMessage)
      .mapLeft { CoInsuredError.GenericError(it.message) }
      .bind()

    val contract = result.currentMember.activeContracts.firstOrNull { it.id == contractId }

    ensureNotNull(contract) {
      CoInsuredError.ContractNotFound
    }

    val coInsuredOnContract = (contract.coInsured.orEmpty() + contract.coOwners.orEmpty())
      .filter { it.terminatesOn == null }
      .map { it.toCoInsured() }

    val allCoInsured = result.currentMember.activeContracts.flatMap {
      val coInsureds = it.coInsured?.map { it.toCoInsured() }.orEmpty()
      val coOwners = it.coOwners?.map { it.toCoInsured() }.orEmpty()
      coInsureds + coOwners
    }
      .filter { !it.hasMissingInfo }
      .filter { coinsured -> coInsuredOnContract.any { it.id == coinsured.id} }

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

  private fun CoInsuredFragment.toCoInsured() = CoInsured(
    firstName = firstName,
    lastName = lastName,
    birthDate = birthdate,
    ssn = ssn,
    hasMissingInfo = hasMissingInfo,
    activatesOn = activatesOn,
    terminatesOn = terminatesOn,
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
