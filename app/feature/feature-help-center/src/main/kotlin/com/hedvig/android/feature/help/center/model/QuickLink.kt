package com.hedvig.android.feature.help.center.model

import com.hedvig.android.feature.help.center.data.QuickLinkDestination

sealed interface QuickAction {
  val titleRes: Int
  val hintTextRes: Int

  // todo: do not really have them anymore; but may be useful later on?
  data class MultiSelectQuickLink(
    override val titleRes: Int,
    override val hintTextRes: Int,
    val links: List<QuickLinkForMultiSelect>,
  ) : QuickAction {
    data class QuickLinkForMultiSelect(
      val displayName: String,
      val quickLinkDestination: QuickLinkDestination,
    )
  }

  data class StandaloneQuickLink(
    override val titleRes: Int,
    override val hintTextRes: Int,
    val quickLinkDestination: QuickLinkDestination,
  ) : QuickAction

  data class MultiSelectExpandedLink(
    override val titleRes: Int,
    override val hintTextRes: Int,
    val links: List<StandaloneQuickLink>,
  ) : QuickAction
}
