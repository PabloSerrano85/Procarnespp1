package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "rocca_session")

class SessionPreferences(private val context: Context) {
    private val keyEmail = stringPreferencesKey("logged_in_email")

    val loggedInEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[keyEmail]
    }

    suspend fun setLoggedInEmail(email: String?) {
        context.dataStore.edit { prefs ->
            if (email.isNullOrBlank()) {
                prefs.remove(keyEmail)
            } else {
                prefs[keyEmail] = email
            }
        }
    }
}
