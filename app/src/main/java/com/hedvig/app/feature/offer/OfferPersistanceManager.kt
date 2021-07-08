package com.hedvig.app.feature.offer

import android.content.SharedPreferences

interface OfferPersistenceManager {
    fun persistQuoteIds(ids: Set<String>)
    fun removeAllQuoteIds()
    fun getPersistedQuoteIds(): Set<String>
}

class OfferPersistenceManagerImpl(
    private val sharedPreferences: SharedPreferences
) : OfferPersistenceManager {

    private val quoteIdKey = "quote_id"

    override fun persistQuoteIds(ids: Set<String>) {
        sharedPreferences.edit()
            .putStringSet(quoteIdKey, ids)
            .apply()
    }

    override fun removeAllQuoteIds() {
        sharedPreferences.edit()
            .remove(quoteIdKey)
            .apply()
    }

    override fun getPersistedQuoteIds(): Set<String> {
        return sharedPreferences.getStringSet(quoteIdKey, emptySet()) ?: emptySet()
    }
}
