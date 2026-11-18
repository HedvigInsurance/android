package com.hedvig.android.feature.editcoinsured.navigation

import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.EditCoInsuredDestination
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

/** Replaces the triage screen with the step that edits the co-insured or co-owners of [insurance]. */
internal fun Backstack.navigateToTriageStep(insurance: InsuranceForEditOrAddCoInsured) {
  navigateFromTriage(insurance.toNavKey())
}

private fun InsuranceForEditOrAddCoInsured.toNavKey(): HedvigNavKey = when (destination) {
  EditCoInsuredDestination.MISSING_INFO -> CoInsuredAddInfoKey(id, type)
  EditCoInsuredDestination.ADD_OR_REMOVE -> CoInsuredAddOrRemoveKey(id, type)
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
