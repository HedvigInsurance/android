package com.hedvig.android.notification.badge.data.crosssell.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.logger.logcat
import com.hedvig.android.notification.badge.data.crosssell.CrossSellBadgeType
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class CrossSellHomeNotificationServiceProvider(
  override val demoManager: DemoManager,
  override val demoImpl: CrossSellHomeNotificationService,
  override val prodImpl: CrossSellHomeNotificationService,
) : ProdOrDemoProvider<CrossSellHomeNotificationService>

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

internal class CrossSellHomeNotificationServiceImpl(
  private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
  private val dataStore: DataStore<Preferences>,
) : CrossSellHomeNotificationService {
  private val crossSellRedDotBadgeType = CrossSellBadgeType.InsuranceFragmentCard

  override fun showRedDotNotification(): Flow<Boolean> {
    // return flowOf(true) //todo: remove mock
    return crossSellNotificationBadgeService.showNotification(crossSellRedDotBadgeType)
  }

  override fun getLastEpochDayNewRecommendationNotificationWasShown(): Flow<Long?> {
    return dataStore
      .data
      .map { preferences ->
        val result = preferences[SHARED_PREFERENCE_CROSS_SELL_RECOMMENDATION]
        logcat { "Mariia: getLastEpochDayNewRecommendationNotificationWasShown result: $result " }
        result
      }
      .distinctUntilChanged()
    //  return flowOf(null) //todo: remove mock
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
