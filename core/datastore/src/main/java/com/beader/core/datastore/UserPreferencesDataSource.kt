package com.beader.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Thin wrapper around Preferences DataStore. Callers never touch
 * [Preferences] keys directly — everything is exposed as typed properties.
 */
class UserPreferencesDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val isDynamicColorEnabled: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[DYNAMIC_COLOR_KEY] ?: true
            }

        suspend fun setDynamicColorEnabled(enabled: Boolean) {
            dataStore.edit { prefs -> prefs[DYNAMIC_COLOR_KEY] = enabled }
        }

        private companion object {
            val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color_enabled")
        }
    }
