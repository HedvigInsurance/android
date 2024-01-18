package com.hedvig.android.feature.help.center.model

import com.kiwi.navigationcompose.typed.Destination

sealed interface QuickAction {
  val titleRes: Int

  data class MultiSelectQuickLink(
    override val titleRes: Int,
    val links: List<QuickLink>,
  ) : QuickAction

  data class QuickLink(
    override val titleRes: Int,
    val displayName: String?,
    val destination: Destination,
  ) : QuickAction
}
