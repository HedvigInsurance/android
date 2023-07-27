package com.hedvig.android.notification.badge.data.storage

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

internal sealed interface NotificationBadge<T> {
  val preferencesKey: Preferences.Key<T>

  object CrossSellInsuranceFragmentCard : NotificationBadge<Set<String>> {
    override val preferencesKey = stringSetPreferencesKey("SEEN_CROSS_SELLS_INSURANCE_FRAGMENT_CARD")
  }

  sealed interface BottomNav<T> : NotificationBadge<T> {
    object CrossSellOnInsuranceScreen : BottomNav<Set<String>> {
      override val preferencesKey = stringSetPreferencesKey("SEEN_CROSS_SELLS")
    }

    object ReferralCampaign : BottomNav<Boolean> {
      override val preferencesKey = booleanPreferencesKey("SEEN_REFERRAL_CAMPAIGN")
    }
  }
}
