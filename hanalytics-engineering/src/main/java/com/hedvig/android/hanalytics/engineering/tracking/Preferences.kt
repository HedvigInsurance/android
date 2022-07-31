package com.hedvig.android.hanalytics.engineering.tracking

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

internal val SHOULD_SHOW_NOTIFICATION = booleanPreferencesKey("SHOULD_SHOW_NOTIFICATION")
internal val Context.trackingPreferences by preferencesDataStore("tracking_preferences")
