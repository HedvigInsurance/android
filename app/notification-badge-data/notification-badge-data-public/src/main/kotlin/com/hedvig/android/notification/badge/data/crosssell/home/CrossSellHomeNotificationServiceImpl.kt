package com.hedvig.android.notification.badge.data.crosssell.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingCrossSellHomeNotificationService(
  private val demoManager: DemoManager,
  private val prodImpl: CrossSellHomeNotificationServiceImpl,
  private val demoImpl: DemoCrossSellHomeNotificationService,
) : CrossSellHomeNotificationService {
  override fun showRedDotNotification() = flow {
    emitAll(pick().showRedDotNotification())
  }

  override fun getLastEpochDayNewRecommendationNotificationWasShown() = flow {
    emitAll(pick().getLastEpochDayNewRecommendationNotificationWasShown())
  }

  override suspend fun markAsSeen() = pick().markAsSeen()

  override suspend fun setLastEpochDayNewRecommendationNotificationWasShown(epochDay: Long) =
    pick().setLastEpochDayNewRecommendationNotificationWasShown(epochDay)

  private suspend fun pick(): CrossSellHomeNotificationService =
    if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}

@Inject
internal class DemoCrossSellHomeNotificationService() : CrossSellHomeNotificationService {
  var showNotification = true

  override fun showRedDotNotification(): Flow<Boolean> {
    return flowOf(showNotification)
  }

  override fun getLastEpochDayNewRecommendationNotificationWasShown(): Flow<Long?> {
    return flowOf(null)
  }

  override suspend fun markAsSeen() {
    showNotification = false
  }

  override suspend fun setLastEpochDayNewRecommendationNotificationWasShown(epochDay: Long) {
  }
}

interface CrossSellHomeNotificationService {
  fun showRedDotNotification(): Flow<Boolean>

  fun getLastEpochDayNewRecommendationNotificationWasShown(): Flow<Long?>

  suspend fun markAsSeen()

  suspend fun setLastEpochDayNewRecommendationNotificationWasShown(epochDay: Long)
}

@Inject
@SingleIn(AppScope::class)
internal class CrossSellHomeNotificationServiceImpl(
  private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
  private val dataStore: DataStore<Preferences>,
) : CrossSellHomeNotificationService {
  private val crossSellRedDotBadgeType = NotificationBadge.CrossSellInsuranceFragmentCard

  override fun showRedDotNotification(): Flow<Boolean> {
    return crossSellNotificationBadgeService.showNotification(crossSellRedDotBadgeType)
  }

  override fun getLastEpochDayNewRecommendationNotificationWasShown(): Flow<Long?> {
    return dataStore
      .data
      .map { preferences ->
        val result = preferences[SHARED_PREFERENCE_CROSS_SELL_RECOMMENDATION]
        result
      }
      .distinctUntilChanged()
  }

  override suspend fun markAsSeen() {
    crossSellNotificationBadgeService.markCurrentCrossSellsAsSeen(crossSellRedDotBadgeType)
  }

  override suspend fun setLastEpochDayNewRecommendationNotificationWasShown(epochDay: Long) {
    dataStore.edit { preferences ->
      preferences[SHARED_PREFERENCE_CROSS_SELL_RECOMMENDATION] = epochDay
    }
  }

  private val SHARED_PREFERENCE_CROSS_SELL_RECOMMENDATION = longPreferencesKey("CROSS_SELL_TOOLTIP_LAST_SHOW_EPOCH_DAY")
}
