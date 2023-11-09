package com.hedvig.android.core.common.android

import android.os.Parcelable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import octopus.fragment.IconsFragment

@Serializable
@Parcelize
data class ThemedIconUrls(
  private val darkUrl: String,
  private val lightUrl: String,
) : Parcelable {
  val themedIcon: String
    @Composable
    get() = if (isSystemInDarkTheme()) {
      darkUrl
    } else {
      lightUrl
    }

  companion object {
    fun from(fragment: IconsFragment): ThemedIconUrls = ThemedIconUrls(
      fragment.variants.dark.svgUrl,
      fragment.variants.light.svgUrl,
    )
  }
}
