package com.divyanshu_in.multiuserlocationsharingapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dataStore")

val USERNAME = stringPreferencesKey("username")

val Context.getUsername
    get() = this.dataStore.data.map { preferences -> preferences[USERNAME] }

suspend fun Context.saveUsername(userName: String) {
    this.dataStore.edit { settings ->
        settings[USERNAME] = userName
    }
}