package com.hedvig.android.feature.help.center.model

import com.hedvig.android.feature.help.center.data.QuickLinkDestination

sealed interface QuickAction {
  val titleRes: Int
  val hintTextRes: Int

  data class MultiSelectQuickLink(
    override val titleRes: Int,
    override val hintTextRes: Int,
    val links: List<QuickLinkForMultiSelect>,
  ) : QuickAction

  data class StandaloneQuickLink(
    override val titleRes: Int,
    override val hintTextRes: Int,
    val quickLinkDestination: QuickLinkDestination,
  ) : QuickAction
}

data class QuickLinkForMultiSelect(
  val titleRes: Int,
  val displayName: String?,
  val hintTextRes: Int,
  val quickLinkDestination: QuickLinkDestination,
)
