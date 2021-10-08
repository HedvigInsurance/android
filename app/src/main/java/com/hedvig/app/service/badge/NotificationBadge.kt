package com.hedvig.app.service.badge

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey

sealed class NotificationBadge<T>(val key: Preferences.Key<T>) {

    object CrossSellInsuranceFragmentCard : NotificationBadge<Set<String>>(
        stringSetPreferencesKey("SEEN_CROSS_SELLS_INSURANCE_FRAGMENT_CARD")
    )

    sealed class BottomNav<T>(key: Preferences.Key<T>) : NotificationBadge<T>(key) {
        object CrossSellOnInsuranceFragment : BottomNav<Set<String>>(stringSetPreferencesKey("SEEN_CROSS_SELLS"))
    }
}
