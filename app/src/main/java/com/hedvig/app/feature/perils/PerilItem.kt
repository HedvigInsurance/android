package com.hedvig.app.feature.perils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class PerilItem : Parcelable {
    sealed class Header : PerilItem() {
        @Parcelize
        data class CoversSuffix(val displayName: String) : Header()

        @Parcelize
        data class Simple(val displayName: String) : Header()
    }

    @Parcelize
    data class Peril(val inner: com.hedvig.app.feature.perils.Peril) : PerilItem()
}
