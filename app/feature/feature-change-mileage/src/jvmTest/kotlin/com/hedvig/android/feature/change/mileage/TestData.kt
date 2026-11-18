package com.hedvig.android.feature.change.mileage

import app.cash.turbine.Turbine
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.change.mileage.data.ChangeMileageIntent
import com.hedvig.android.feature.change.mileage.data.CommitMileageChangeUseCase
import com.hedvig.android.feature.change.mileage.data.CreateMileageIntentError
import com.hedvig.android.feature.change.mileage.data.CreateMileageIntentUseCase
import com.hedvig.android.feature.change.mileage.data.GetMileageContractsUseCase
import com.hedvig.android.feature.change.mileage.data.MileageContract
import com.hedvig.android.feature.change.mileage.data.MileageQuote
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.datetime.LocalDate

internal class TestBackstack(
  override val entries: MutableList<HedvigNavKey> = mutableListOf(),
) : Backstack

internal class FakeGetMileageContractsUseCase : GetMileageContractsUseCase {
  val turbine = Turbine<Either<ErrorMessage, List<MileageContract>>>()

  override suspend fun invoke(): Either<ErrorMessage, List<MileageContract>> = turbine.awaitItem()
}

internal class FakeCreateMileageIntentUseCase : CreateMileageIntentUseCase {
  val turbine = Turbine<Either<CreateMileageIntentError, ChangeMileageIntent>>()

  override suspend fun invoke(contractId: String): Either<CreateMileageIntentError, ChangeMileageIntent> =
    turbine.awaitItem()
}

internal class FakeCommitMileageChangeUseCase : CommitMileageChangeUseCase {
  val turbine = Turbine<Either<ErrorMessage, Unit>>()

  override suspend fun invoke(quoteId: String): Either<ErrorMessage, Unit> = turbine.awaitItem()
}

internal fun testContract(id: String) = MileageContract(
  id = id,
  displayName = "Bilförsäkring Helförsäkring",
  exposureDisplayName = "ABC 123 • Volvo XC40",
  contractGroup = ContractGroup.CAR,
)

private fun cost(amount: Double): ItemCost {
  val money = UiMoney(amount, UiCurrencyCode.SEK)
  return ItemCost(monthlyNet = money, monthlyGross = money, discounts = emptyList())
}

internal val lowerQuote = MileageQuote(
  id = "lower",
  displayName = "1 000 mil",
  isCurrent = false,
  totalCost = cost(371.0),
  infoMessage = "Compensation can be lowered",
  displayItems = emptyList(),
)

internal val currentQuote = MileageQuote(
  id = "current",
  displayName = "1 500 mil",
  isCurrent = true,
  totalCost = cost(412.0),
  infoMessage = null,
  displayItems = emptyList(),
)

internal val testIntent = ChangeMileageIntent(
  activationDate = LocalDate(2026, 11, 17),
  currentTotalCost = cost(412.0),
  quotes = listOf(lowerQuote, currentQuote),
)
