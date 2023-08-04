package com.example.flashtranslator.utils

import android.content.Context
import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.flashtranslator.DATA_STORE_SOURCE_TARGET_LANGUAGES

fun View.visible(v: Boolean) {
    visibility = if(v) View.VISIBLE else View.GONE
}

val Context.obtainLanguageSourceTargetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATA_STORE_SOURCE_TARGET_LANGUAGES
)