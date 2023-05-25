package com.hedvig.app.util.apollo

import android.content.Context
import android.os.Parcelable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.hedvig.app.util.extensions.isDarkThemeActive
import giraffe.fragment.IconVariantsFragment
import kotlinx.parcelize.Parcelize

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
