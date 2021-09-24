package com.hedvig.app.feature.loggedin.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TabNotificationService @Inject constructor(
    private val getCrossSellsUseCase: GetCrossSellsUseCase,
    private val dataStore: DataStore<Preferences>,
) {
    private val seenCrossSells = dataStore
        .data
        .map { preferences ->
            preferences[SEEN_CROSS_SELLS_KEY] ?: emptySet()
        }

    suspend fun load(): Flow<Set<LoggedInTabs>> {
        val potentialCrossSells = getCrossSellsUseCase.invoke()

        return seenCrossSells
            .map { seenCrossSells ->
                if ((potentialCrossSells subtract seenCrossSells).isNotEmpty()) {
                    setOf(LoggedInTabs.INSURANCE)
                } else {
                    emptySet()
                }
            }
    }

    suspend fun visitTab(tab: LoggedInTabs) {
        if (tab == LoggedInTabs.INSURANCE) {
            markCurrentCrossSellsAsSeen()
        }
    }

    private suspend fun markCurrentCrossSellsAsSeen() {
        val crossSells = getCrossSellsUseCase.invoke()
        dataStore
            .edit { preferences ->
                preferences[SEEN_CROSS_SELLS_KEY] =
                    (preferences[SEEN_CROSS_SELLS_KEY] ?: emptySet()) + crossSells
            }
    }

    companion object {
        val SEEN_CROSS_SELLS_KEY = stringSetPreferencesKey("SEEN_CROSS_SELLS")
    }
}
