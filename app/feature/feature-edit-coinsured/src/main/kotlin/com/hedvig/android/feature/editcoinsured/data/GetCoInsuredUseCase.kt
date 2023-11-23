package com.hedvig.android.feature.editcoinsured.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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

    val currentCoInsured = contract.let {
      it.currentAgreement.coInsured?.map {
        CoInsured(
          it.firstName,
          it.lastName,
          it.birthdate,
          it.ssn,
          it.hasMissingInfo,
        )
      }
    }

    CoInsuredResult(
      member = Member(
        firstName = result.currentMember.firstName,
        lastName = result.currentMember.lastName,
        ssn = result.currentMember.ssn,
      ),
      coInsured = currentCoInsured?.toImmutableList() ?: persistentListOf(),
    )
  }
}

internal sealed interface CoInsuredError {
  data class GenericError(val message: String?) : CoInsuredError

  data object ContractNotFound : CoInsuredError
}

internal data class CoInsuredResult(
  val member: Member,
  val coInsured: ImmutableList<CoInsured>,
)
