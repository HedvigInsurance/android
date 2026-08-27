package com.hedvig.android.memberquickactions

import org.jetbrains.compose.resources.StringResource

sealed interface QuickAction {
  val titleRes: StringResource
  val hintTextRes: StringResource

  /** Shorter title for the compact home tile; falls back to [titleRes] when no shorter copy exists. */
  val shortTitleRes: StringResource

  data class StandaloneQuickLink(
    override val titleRes: StringResource,
    override val hintTextRes: StringResource,
    val quickLinkDestination: QuickLinkDestination,
    override val shortTitleRes: StringResource = titleRes,
  ) : QuickAction

  data class MultiSelectExpandedLink(
    override val titleRes: StringResource,
    override val hintTextRes: StringResource,
    val links: List<StandaloneQuickLink>,
    override val shortTitleRes: StringResource = titleRes,
  ) : QuickAction
}
