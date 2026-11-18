package com.hedvig.android.feature.change.mileage.data

import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.contract.ContractGroup
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal data class MileageContract(
  val id: String,
  val displayName: String,
  val exposureDisplayName: String,
  val contractGroup: ContractGroup,
)

internal data class ChangeMileageIntent(
  val activationDate: LocalDate,
  val currentTotalCost: ItemCost,
  /** Sorted ascending by mileage, exactly one has [MileageQuote.isCurrent] set. */
  val quotes: List<MileageQuote>,
)

@Serializable
internal data class MileageQuote(
  val id: String,
  val displayName: String,
  val isCurrent: Boolean,
  val totalCost: ItemCost,
  val displayItems: List<MileageDisplayItem>,
)

@Serializable
internal data class MileageDisplayItem(
  val title: String,
  val value: String,
  /** Set only when the chosen quote changes this row, shown struck through next to [value]. */
  val previousValue: String?,
)

internal sealed interface CreateMileageIntentError {
  /** A message from the backend explaining why the mileage can't be changed, meant to be shown as is. */
  data class UserError(val message: String) : CreateMileageIntentError

  data object General : CreateMileageIntentError
}
