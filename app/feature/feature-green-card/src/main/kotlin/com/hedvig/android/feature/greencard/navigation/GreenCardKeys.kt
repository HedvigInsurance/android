package com.hedvig.android.feature.greencard.navigation

import com.hedvig.android.feature.greencard.data.GreenCardUrl
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object GreenCardKey : HedvigNavKey

/**
 * [contractId] is the contract the flow continues with, or null when the member has to choose one first.
 */
@Serializable
internal data class GreenCardImportantInfoKey(
  val contractId: String?,
) : HedvigNavKey

@Serializable
internal data object GreenCardChooseContractKey : HedvigNavKey

@Serializable
internal data class GreenCardDateInputKey(
  val contractId: String,
) : HedvigNavKey

@Serializable
internal data class GreenCardCreatedKey(
  val greenCardUrl: GreenCardUrl,
) : HedvigNavKey
