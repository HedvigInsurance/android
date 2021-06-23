package com.hedvig.app.feature.perils

import com.hedvig.android.owldroid.type.TypeOfContract

sealed class PerilItem {
    data class Header(val typeOfContract: TypeOfContract) : PerilItem()

    data class Peril(val inner: com.hedvig.app.feature.perils.Peril) : PerilItem()
}
