package com.hedvig.android.market

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MarketStorage(
  private val datastore: DataStore<Preferences>,
) {
  val market: Flow<Market?> = datastore.data
    .map { preferences ->
      val storedMarket: Market? = preferences[marketPreferencesKey]?.let { Market.valueOf(it) }
      if (storedMarket != null) {
        return@map storedMarket
      } else {
        logcat { "No market stored in datastore" }
        return@map null
      }
    }

  suspend fun setMarket(market: Market) {
    datastore.edit {
      it[marketPreferencesKey] = market.name
    }
  }

  companion object {
    private val marketPreferencesKey = stringPreferencesKey("com.hedvig.android.market.Market")
  }
}
