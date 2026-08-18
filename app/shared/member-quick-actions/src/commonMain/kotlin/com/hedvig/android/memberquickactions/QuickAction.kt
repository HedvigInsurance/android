package com.hedvig.android.memberquickactions

import org.jetbrains.compose.resources.StringResource

sealed interface QuickAction {
  val titleRes: StringResource
  val hintTextRes: StringResource

  data class StandaloneQuickLink(
    override val titleRes: StringResource,
    override val hintTextRes: StringResource,
    val quickLinkDestination: QuickLinkDestination,
  ) : QuickAction

  data class MultiSelectExpandedLink(
    override val titleRes: StringResource,
    override val hintTextRes: StringResource,
    val links: List<StandaloneQuickLink>,
  ) : QuickAction
}
