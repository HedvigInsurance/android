package com.hedvig.app.feature.perils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class PerilItem : Parcelable {
    @Parcelize
    data class Header(val displayName: String) : PerilItem()

    @Parcelize
    data class Peril(val inner: com.hedvig.app.feature.perils.Peril) : PerilItem()
}
