package com.hedvig.android.feature.editcoinsured.navigation

import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoInsuredAddInfoKey(
  val contractId: String,
  val type: CoInsuredFlowType,
) : HedvigNavKey

@Serializable
data class CoInsuredAddOrRemoveKey(
  val contractId: String,
  val type: CoInsuredFlowType,
) : HedvigNavKey

@Serializable
data class EditCoInsuredTriageKey(
  @SerialName("contractId")
  val contractId: String? = null,
  val type: CoInsuredFlowType = CoInsuredFlowType.CoInsured,
) : HedvigNavKey

@Serializable
internal data class EditCoOwnersTriageDeepLinkKey(
  @SerialName("contractId")
  val contractId: String? = null,
) : HedvigNavKey

@Serializable
internal data class EditCoInsuredSuccessKey(val date: LocalDate, val type: CoInsuredFlowType) : HedvigNavKey

/**
 * The triage presenter serves both the normal [EditCoInsuredTriageKey] and the deep-link
 * [EditCoOwnersTriageDeepLinkKey], so the pop anchor is resolved at navigation time.
 */
internal fun Backstack.navigateFromTriage(destination: HedvigNavKey) {
  if (findLastOrNull<EditCoInsuredTriageKey>() != null) {
    navigateAndPopUpTo<EditCoInsuredTriageKey>(destination, inclusive = true)
  } else {
    navigateAndPopUpTo<EditCoOwnersTriageDeepLinkKey>(destination, inclusive = true)
  }
}

/**
 * The edit presenter serves both [CoInsuredAddInfoKey] and [CoInsuredAddOrRemoveKey], so the pop
 * anchor is resolved at navigation time.
 */
internal fun Backstack.navigateToEditCoInsuredSuccess(successKey: EditCoInsuredSuccessKey) {
  if (findLastOrNull<CoInsuredAddInfoKey>() != null) {
    navigateAndPopUpTo<CoInsuredAddInfoKey>(successKey, inclusive = true)
  } else {
    navigateAndPopUpTo<CoInsuredAddOrRemoveKey>(successKey, inclusive = true)
  }
}
