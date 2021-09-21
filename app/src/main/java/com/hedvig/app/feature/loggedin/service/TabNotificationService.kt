package com.hedvig.app.feature.loggedin.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.hedvig.android.owldroid.graphql.CrossSellsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.util.apollo.QueryResult
import e
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TabNotificationService(
    private val getCrossSellsUseCase: GetCrossSellsUseCase,
    private val dataStore: DataStore<Preferences>,
) {
    private val seenCrossSells = dataStore
        .data
        .map { preferences ->
            preferences[SEEN_CROSS_SELLS_KEY]
        }

    suspend fun load(): Flow<Set<LoggedInTabs>> {
        val potentialCrossSells = when (val potentialCrossSellsResult = getCrossSellsUseCase.invoke()) {
            is QueryResult.Success -> {
                getCrossSells(potentialCrossSellsResult.data)
            }
            is QueryResult.Error -> {
                e { "Error when loading potential cross-sells: ${potentialCrossSellsResult.message}" }
                emptySet()
            }
        }

        return seenCrossSells
            .map {
                if ((potentialCrossSells subtract (it ?: emptySet())).isNotEmpty()) {
                    setOf(LoggedInTabs.INSURANCE)
                } else {
                    emptySet()
                }
            }
    }

    private fun getCrossSells(
        crossSellData: CrossSellsQuery.Data
    ) = crossSellData
        .activeContractBundles
        .flatMap { contractBundle ->
            contractBundle
                .potentialCrossSells
                .map { it.contractType.toString() }
        }
        .toSet()

    suspend fun visitTab(tab: LoggedInTabs) {
        if (tab == LoggedInTabs.INSURANCE) {
            markCurrentCrossSellsAsSeen()
        }
    }

    private suspend fun markCurrentCrossSellsAsSeen() {
        val currentCrossSellsResult = getCrossSellsUseCase.invoke()
        if (currentCrossSellsResult !is QueryResult.Success) {
            (currentCrossSellsResult as? QueryResult.Error)?.message?.let {
                e { "Error when attempting to load current cross-sells: $it" }
            }
            return
        }
        val crossSells = getCrossSells(currentCrossSellsResult.data)
        dataStore
            .edit { preferences ->
                preferences[SEEN_CROSS_SELLS_KEY] =
                    preferences[SEEN_CROSS_SELLS_KEY] ?: emptySet<String>() + crossSells
            }
    }

    companion object {
        val SEEN_CROSS_SELLS_KEY = stringSetPreferencesKey("SEEN_CROSS_SELLS")
    }
}

