package com.example.plomaap.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Declaración Singleton de DataStore para evitar el error "There are multiple DataStores active"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "plomaap_prefs")
