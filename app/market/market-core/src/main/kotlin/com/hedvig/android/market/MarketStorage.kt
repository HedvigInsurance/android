package com.hedvig.android.market

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MarketStorage(
  private val datastore: DataStore<Preferences>,
  context: Context,
) {
  private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  val market: Flow<Market> = datastore.data
    .map { preferences ->
      val storedMarket: Market? = preferences[marketPreferencesKey]?.let { Market.valueOf(it) }
      if (storedMarket != null) {
        return@map storedMarket
      }
      // Remove sharedPrefs code when we no longer need to migrate old users, and just fallback to SE instead
      val sharedPrefsStoredMarket = sharedPreferences
        .getString(MARKET_SHARED_PREF, null)
        ?.let { Market.valueOf(it) }
      if (sharedPrefsStoredMarket == null) {
        return@map Market.SE // Fallback to SE in case there was nothing in the datastore
      }
      datastore.edit {
        it[marketPreferencesKey] = sharedPrefsStoredMarket.name
      }
      sharedPreferences.edit()
        .remove(MARKET_SHARED_PREF)
        .apply()
      sharedPrefsStoredMarket
    }

  suspend fun setMarket(market: Market) {
    datastore.edit {
      it[marketPreferencesKey] = market.name
    }
  }

  companion object {
    private const val MARKET_SHARED_PREF = "MARKET_SHARED_PREF"
    private val marketPreferencesKey = stringPreferencesKey("com.hedvig.android.market.Market")
  }
}
