package com.hedvig.android.core.common.android

import android.content.Context
import android.os.Parcelable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import giraffe.fragment.IconVariantsFragment
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class ThemedIconUrls(
  private val darkUrl: String,
  private val lightUrl: String,
) : Parcelable {

  fun iconByTheme(context: Context) = if (context.isDarkThemeActive) {
    darkUrl
  } else {
    lightUrl
  }

  val themedIcon: String
    @Composable
    get() = if (isSystemInDarkTheme()) {
      darkUrl
    } else {
      lightUrl
    }

  companion object {
    fun from(variants: IconVariantsFragment) =
      ThemedIconUrls(
        variants.dark.svgUrl,
        variants.light.svgUrl,
      )
  }
}
