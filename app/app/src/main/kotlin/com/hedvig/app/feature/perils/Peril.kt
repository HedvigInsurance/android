package com.hedvig.app.feature.perils

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import giraffe.fragment.PerilFragmentV2
import kotlinx.parcelize.Parcelize
import octopus.CrossSalesQuery

@Parcelize
@Immutable
data class Peril(
  val title: String,
  val description: String,
  val darkUrl: String?,
  val lightUrl: String?,
  val colorCode: Long?, // A valid color number in base 16, or null.
  val exception: List<String>,
  val covered: List<String>,
  val info: String,
) : Parcelable {
  companion object {
    fun from(fragment: PerilFragmentV2) = Peril(
      title = fragment.title,
      description = fragment.description,
      darkUrl = fragment.icon.variants.dark.svgUrl,
      lightUrl = fragment.icon.variants.light.svgUrl,
      colorCode = null,
      exception = fragment.exceptions,
      covered = fragment.covered,
      info = fragment.info,
    )

    fun from(peril: CrossSalesQuery.Data.CurrentMember.CrossSell.ProductVariant.Peril) = Peril(
      title = peril.title,
      description = peril.description,
      exception = listOf(),
      covered = listOf(),
      info = peril.info,
      colorCode = peril.colorCode?.dropWhile { it == '#' }?.takeIf { it.length == 8 }?.toLongOrNull(16),
      darkUrl = null,
      lightUrl = null,
    )
  }
}
