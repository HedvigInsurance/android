package com.hedvig.app.feature.profile.ui.payment

import com.hedvig.android.owldroid.graphql.ProfileQuery

sealed class ChargeWrapper {
    object Title : ChargeWrapper()
    data class Header(val year: Int) : ChargeWrapper()
    data class Item(val charge: ProfileQuery.ChargeHistory) : ChargeWrapper()
}
