package com.hedvig.app.util.apollo

import android.content.Context
import android.os.Parcelable
import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.app.util.extensions.isDarkThemeActive
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ThemedIconUrls(
    val darkUrl: String,
    val lightUrl: String
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
                variants.light.svgUrl
            )
    }
}
