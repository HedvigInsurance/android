package com.hedvig.android.market

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

interface MarketManager {
  val enabledMarkets: List<Market>
  val market: Market?
  suspend fun setMarket(market: Market)
  fun observeMarket(): Flow<Market?>
}

internal class MarketManagerImpl(
  isDebug: Boolean,
  private val dataStore: DataStore<Preferences>,
) : MarketManager {

  override val enabledMarkets = listOfNotNull(
    Market.SE,
    Market.NO,
    Market.DK,
    if (isDebug) Market.FR else null,
  )
  override val market: Market?
    get() = runBlocking { observeMarket().first() }

  override fun observeMarket(): Flow<Market?> {
    return dataStore.data.map { preferences ->
      val market = preferences[marketPreferenceKey] ?: return@map null
      Market.valueOf(market)
    }
  }

  override suspend fun setMarket(market: Market) {
    dataStore.edit { preferences ->
      preferences[marketPreferenceKey] = market.name
    }
  }

  companion object {
    private val marketPreferenceKey = stringPreferencesKey("com.hedvig.market.storage.MARKET")
  }
}
