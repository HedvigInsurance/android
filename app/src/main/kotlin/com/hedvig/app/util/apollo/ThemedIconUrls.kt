package com.hedvig.app.util.apollo

import android.content.Context
import android.os.Parcelable
import com.hedvig.android.apollo.graphql.fragment.IconVariantsFragment
import com.hedvig.app.util.extensions.isDarkThemeActive
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

  companion object {
    fun from(variants: IconVariantsFragment) =
      ThemedIconUrls(
        variants.dark.svgUrl,
        variants.light.svgUrl,
      )
  }
}
