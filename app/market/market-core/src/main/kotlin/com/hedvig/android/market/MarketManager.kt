package com.hedvig.android.market

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface MarketManager {
  val market: StateFlow<Market>
  suspend fun setMarket(market: Market)
}

internal class MarketManagerImpl(
  private val datastore: DataStore<Preferences>,
  context: Context,
  coroutineScope: CoroutineScope,
) : MarketManager {
  private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  override val market: StateFlow<Market> = datastore.data
    .map { preferences ->
      val storedMarket: Market? = preferences[marketPreferencesKey]?.let { Market.valueOf(it) }
      if (storedMarket != null) {
        return@map storedMarket
      }
      // Remove sharedPrefs code when we no longer need to migrate old users, and just fallback to SE instead
      val sharedPrefsStoredMarket = sharedPreferences
        .getString(Market.MARKET_SHARED_PREF, null)
        ?.let { Market.valueOf(it) }
      if (sharedPrefsStoredMarket == null) {
        return@map Market.SE // Fallback to SE in case there was nothing in the datastore
      }
      datastore.edit {
        it[marketPreferencesKey] = sharedPrefsStoredMarket.name
      }
      sharedPreferences.edit()
        .remove(Market.MARKET_SHARED_PREF)
        .apply()
      sharedPrefsStoredMarket
    }
    .stateIn(
      coroutineScope,
      SharingStarted.Eagerly,
      Market.SE,
    )

  override suspend fun setMarket(market: Market) {
    datastore.edit {
      it[marketPreferencesKey] = market.name
    }
  }

  companion object {
    val marketPreferencesKey = stringPreferencesKey("com.hedvig.android.market.Market")
  }
}
