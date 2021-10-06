package com.hedvig.app.service.badge

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.hedvig.android.owldroid.type.TypeOfContract

sealed class NotificationBadge(val key: Preferences.Key<Boolean>) {
    object CrossSellInsuranceFragmentCard : NotificationBadge(
        booleanPreferencesKey("SEEN_CROSS_SELL_INSURANCE_FRAGMENT_CARD")
    )

    sealed class BottomNav(key: Preferences.Key<Boolean>) : NotificationBadge(key) {
        object CrossSellOnInsuranceFragment : BottomNav(booleanPreferencesKey("SEEN_CROSS_SELLS"))
    }

    companion object {
        fun fromPotentialCrossSells(
            potentialCrossSells: Set<TypeOfContract>
        ): List<BottomNav> =
            potentialCrossSells.mapNotNull { typeOfContract ->
                if (typeOfContract == TypeOfContract.SE_ACCIDENT) {
                    BottomNav.CrossSellOnInsuranceFragment
                } else {
                    null
                }
            }
    }
}
