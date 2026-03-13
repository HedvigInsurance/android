package com.hedvig.android.feature.editcoinsured.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional.Companion.presentIfNotNull
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import octopus.CreateMidtermChangeMutation
import octopus.type.CoInsuredInput
import octopus.type.MidtermChangeIntentCreateInput

internal interface CreateMidtermChangeUseCase {
  suspend fun invoke(contractId: String, coInsured: List<CoInsured>): Either<ErrorMessage, CreateMidtermChangeResult>
}

internal class CreateMidtermChangeUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreateMidtermChangeUseCase {
  override suspend fun invoke(
    contractId: String,
    coInsured: List<CoInsured>,
  ): Either<ErrorMessage, CreateMidtermChangeResult> = either {
    val mutation = CreateMidtermChangeMutation(
      contractId,
      MidtermChangeIntentCreateInput(
        coInsuredInputs = presentIfNotNull(
          coInsured.map {
            CoInsuredInput(
              firstName = presentIfNotNull(it.firstName),
              lastName = presentIfNotNull(it.lastName),
              ssn = presentIfNotNull(it.ssn),
              birthdate = presentIfNotNull(it.birthDate),
            )
          },
        ),
      ),
    )

    val result = apolloClient.mutation(mutation)
      .safeExecute(::ErrorMessage)
      .bind()

    val userError = result.midtermChangeIntentCreate.userError
    if (userError != null) {
      raise(ErrorMessage(userError.message))
    }

    result.midtermChangeIntentCreate.intent?.let {
      CreateMidtermChangeResult(
        id = it.id,
        currentCost = MonthlyCost(
          monthlyGross = UiMoney.fromMoneyFragment(it.currentTotalCost.monthlyGross),
          monthlyNet = UiMoney.fromMoneyFragment(it.currentTotalCost.monthlyNet),
        ),
        newCost = MonthlyCost(
          monthlyGross = UiMoney.fromMoneyFragment(it.newTotalCost.monthlyGross),
          monthlyNet = UiMoney.fromMoneyFragment(it.newTotalCost.monthlyNet),
        ),
        activatedDate = it.activationDate,
        coInsured = coInsured,
        newCostBreakDown = it.newCostBreakdown.map { (displayTitle, displayValue) ->
          displayTitle to displayValue
        },
      )
    } ?: raise(ErrorMessage("No intent"))
  }
}

internal data class CreateMidtermChangeResult(
  val id: String,
  val currentCost: MonthlyCost,
  val newCost: MonthlyCost,
  val newCostBreakDown: List<Pair<String, String>>,
  val activatedDate: LocalDate,
  val coInsured: List<CoInsured>,
)

internal data class MonthlyCost(
  val monthlyGross: UiMoney,
  val monthlyNet: UiMoney,
)
