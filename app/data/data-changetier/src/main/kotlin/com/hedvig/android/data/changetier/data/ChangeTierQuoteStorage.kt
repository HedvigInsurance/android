package com.hedvig.android.data.changetier.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal interface ChangeTierQuoteStorage {
  suspend fun insertAll(quotes: List<TierDeductibleQuote>)

  suspend fun clearAllQuotes()

  suspend fun getOneQuoteById(id: String): TierDeductibleQuote?

  suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote>
}

internal class ChangeTierQuoteStorageImpl(
  private val datastore: DataStore<Preferences>,
) : ChangeTierQuoteStorage {
  override suspend fun insertAll(quotes: List<TierDeductibleQuote>) {
    datastore.edit { preferences ->
      val existingQuotes = preferences[tierDeductibleQuotesKey]?.deserialize() ?: emptyMap()
      val mergedQuotes = existingQuotes.plus(quotes.toQuoteIdKeyedMap())
      preferences[tierDeductibleQuotesKey] = mergedQuotes.serialize()
    }
  }

  override suspend fun clearAllQuotes() {
    datastore.edit { preferences ->
      preferences.remove(tierDeductibleQuotesKey)
    }
  }

  override suspend fun getOneQuoteById(id: String): TierDeductibleQuote? {
    return datastore
      .data
      .map { preferences ->
        preferences[tierDeductibleQuotesKey]?.deserialize()?.get(id)
      }
      .first()
  }

  override suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote> {
    return datastore
      .data
      .map { preferences ->
        preferences[tierDeductibleQuotesKey]?.deserialize()?.filterKeys { key ->
          key in ids
        }
      }
      .map { it?.values?.toList() ?: emptyList() }
      .first()
  }

  companion object {
    private val tierDeductibleQuotesKey: Key<Set<String>> =
      stringSetPreferencesKey("com.hedvig.android.data.changetier.data.tierDeductibleQuotesKey")
  }
}

private fun Set<String>.deserialize(): Map<String, TierDeductibleQuote> {
  return this.map { Json.decodeFromString<TierDeductibleQuote>(it) }.toQuoteIdKeyedMap()
}

private fun Map<String, TierDeductibleQuote>.serialize(): Set<String> {
  return this.values.map { Json.encodeToString<TierDeductibleQuote>(it) }.toSet()
}

private fun List<TierDeductibleQuote>.toQuoteIdKeyedMap(): Map<String, TierDeductibleQuote> {
  return this.associateBy { it.id }
}
