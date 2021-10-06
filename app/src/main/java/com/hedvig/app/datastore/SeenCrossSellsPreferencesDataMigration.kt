package com.hedvig.app.datastore

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.service.badge.NotificationBadge

class SeenCrossSellsPreferencesDataMigration : DataMigration<Preferences> {
    private val oldCrossSellSetKey = stringSetPreferencesKey("SEEN_CROSS_SELLS")

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return try {
            val oldCrossSellStrings = currentData[oldCrossSellSetKey] ?: return false
            return oldCrossSellStrings.isNotEmpty()
        } catch (e: ClassCastException) {
            false
        }
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val crossSellTypes = currentData[oldCrossSellSetKey]!!
        val contractTypes = crossSellTypes.map(TypeOfContract::safeValueOf)

        val mutablePreferences = currentData.toMutablePreferences()
        mutablePreferences -= oldCrossSellSetKey
        for (contractType in contractTypes) {
            when (contractType) {
                TypeOfContract.SE_ACCIDENT -> {
                    @Suppress("ReplaceGetOrSet")
                    mutablePreferences.set(
                        NotificationBadge.BottomNav.CrossSellOnInsuranceFragment.key,
                        true
                    )
                }
                else -> {
                }
            }
        }

        return mutablePreferences.toPreferences()
    }

    override suspend fun cleanUp() {}
}
