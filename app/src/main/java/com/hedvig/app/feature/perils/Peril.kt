package com.hedvig.app.feature.perils

import android.os.Parcelable
import com.hedvig.android.owldroid.fragment.PerilFragment
import kotlinx.parcelize.Parcelize

@Parcelize
data class Peril(
    val title: String,
    val description: String,
    val darkUrl: String,
    val lightUrl: String,
    val exception: List<String>,
    val covered: List<String>,
    val info: String,
) : Parcelable {
    companion object {
        fun from(fragment: PerilFragment) = Peril(
            title = fragment.title,
            description = fragment.description,
            darkUrl = fragment.icon.variants.dark.svgUrl,
            lightUrl = fragment.icon.variants.light.svgUrl,
            exception = fragment.exceptions,
            covered = fragment.covered,
            info = fragment.info
        )
    }
}

