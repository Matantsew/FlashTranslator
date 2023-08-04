package com.example.flashtranslator.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.flashtranslator.Language
import com.example.flashtranslator.SOURCE_LANGUAGE_SPINNER_POSITION_PREF_KEY
import com.example.flashtranslator.SOURCE_LANGUAGE_KEY_PREF_KEY
import com.example.flashtranslator.TARGET_LANGUAGE_KEY_PREF_KEY
import com.example.flashtranslator.TARGET_LANGUAGE_SPINNER_POSITION_PREF_KEY
import com.example.flashtranslator.data.data_source.LanguagesHelper
import com.example.flashtranslator.utils.obtainLanguageSourceTargetDataStore
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagesRepository @Inject constructor() {

     fun getAvailableLanguages() = flow {

        val languageKeys = TranslateLanguage.getAllLanguages()

        languageKeys.forEach { key ->
            emit((Language(key)))
        }
    }

    fun getDownloadedLanguages(readBlock: (languagesList: List<Language>) -> Unit) {

        val downloadedLanguagesModels = LanguagesHelper.getDownloadedLanguagesModels()

        downloadedLanguagesModels.addOnSuccessListener { languages ->
            val languagesList = languages.map { model ->
                Language(model.language)
            }
            readBlock(languagesList)
        }
    }

    fun downloadLanguageModel(languageTag: String) = LanguagesHelper.obtainRemotelyLanguageModel(languageTag)

    suspend fun getSourceLanguagePosition(context: Context) =
        context
            .obtainLanguageSourceTargetDataStore
            .data
            .first()[intPreferencesKey(SOURCE_LANGUAGE_SPINNER_POSITION_PREF_KEY)]

    suspend fun getTargetLanguagePosition(context: Context) =
        context
            .obtainLanguageSourceTargetDataStore
            .data
            .first()[intPreferencesKey(TARGET_LANGUAGE_SPINNER_POSITION_PREF_KEY)]

    suspend fun setSourceLanguagePosition(context: Context, position: Int) {
        context
            .obtainLanguageSourceTargetDataStore
            .edit { preferences ->
                preferences[intPreferencesKey(SOURCE_LANGUAGE_SPINNER_POSITION_PREF_KEY)] = position
            }
    }

    suspend fun setSourceLanguageKey(context: Context, key: String) {
        context
            .obtainLanguageSourceTargetDataStore
            .edit { preferences ->
                preferences[stringPreferencesKey(SOURCE_LANGUAGE_KEY_PREF_KEY)] = key
            }
    }

    suspend fun setTargetLanguagePosition(context: Context, position: Int) {
        context
            .obtainLanguageSourceTargetDataStore
            .edit { preferences ->
                preferences[intPreferencesKey(TARGET_LANGUAGE_SPINNER_POSITION_PREF_KEY)] = position
            }
    }

    suspend fun setTargetLanguageKey(context: Context, key: String) {

        context
            .obtainLanguageSourceTargetDataStore
            .edit { preferences ->
                preferences[stringPreferencesKey(TARGET_LANGUAGE_KEY_PREF_KEY)] = key
            }
    }
}