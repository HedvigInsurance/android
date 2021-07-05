package com.hedvig.app.feature.perils

import android.os.Parcelable
import com.hedvig.android.owldroid.type.TypeOfContract
import kotlinx.parcelize.Parcelize

sealed class PerilItem : Parcelable {
    @Parcelize
    data class Header(val typeOfContract: TypeOfContract) : PerilItem()

    @Parcelize
    data class Peril(val inner: com.hedvig.app.feature.perils.Peril) : PerilItem()
}
