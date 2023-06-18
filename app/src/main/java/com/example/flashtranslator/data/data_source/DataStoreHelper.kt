package com.example.flashtranslator.data.data_source

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import com.example.flashtranslator.DATA_STORE_LANGUAGES
import com.example.flashtranslator.DATA_STORE_LANGUAGE_SOURCE_TARGET

object DataStoreHelper {

    private var languagesDataStore: DataStore<Preferences>? = null
    private var languageSourceTargetDataStore: DataStore<Preferences>? = null

    fun obtainLanguagesDatastore(context: Context): DataStore<Preferences> {
        if(languagesDataStore == null){
            languagesDataStore = context.createDataStore(name = DATA_STORE_LANGUAGES)
        }

        return languagesDataStore as DataStore<Preferences>
    }

    fun obtainLanguageSourceTargetDataStore(context: Context): DataStore<Preferences> {
        if(languageSourceTargetDataStore == null) {
            languageSourceTargetDataStore = context.createDataStore(name = DATA_STORE_LANGUAGE_SOURCE_TARGET)
        }

        return languageSourceTargetDataStore as DataStore<Preferences>
    }
}