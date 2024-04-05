package com.hedvig.android.feature.help.center.model

import com.kiwi.navigationcompose.typed.Destination

sealed interface QuickAction {
  val titleRes: Int
  val hintTextRes: Int

  data class MultiSelectQuickLink(
    override val titleRes: Int,
    override val hintTextRes: Int,
    val links: List<QuickLink>,
  ) : QuickAction

  data class QuickLink(
    override val titleRes: Int,
    val displayName: String?,
    override val hintTextRes: Int,
    val destination: Destination,
  ) : QuickAction
}
