package com.example.latranslator.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.latranslator.Language
import com.example.latranslator.SOURCE_LANGUAGE_KEY_PREF_KEY
import com.example.latranslator.TARGET_LANGUAGE_KEY_PREF_KEY
import com.example.latranslator.data.data_source.LanguagesHelper
import com.example.latranslator.utils.obtainLanguageSourceTargetDataStore
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

    fun downloadLanguageModel(languageTag: String) = LanguagesHelper.downloadRemotelyLanguageModel(languageTag)
    fun deleteLanguageModel(languageTag: String) = LanguagesHelper.deleteRemotelyLanguageModel(languageTag)

    suspend fun setSourceLanguageKey(context: Context, key: String) {
        context
            .obtainLanguageSourceTargetDataStore
            .edit { preferences ->
                preferences[stringPreferencesKey(SOURCE_LANGUAGE_KEY_PREF_KEY)] = key
            }
    }

    suspend fun setTargetLanguageKey(context: Context, key: String) {
        context
            .obtainLanguageSourceTargetDataStore
            .edit { preferences ->
                preferences[stringPreferencesKey(TARGET_LANGUAGE_KEY_PREF_KEY)] = key
            }
    }

    suspend fun getSourceLanguageKey(context: Context) =
        context
            .obtainLanguageSourceTargetDataStore
            .data
            .first()
            .asMap()[stringPreferencesKey(SOURCE_LANGUAGE_KEY_PREF_KEY)] as String?

    suspend fun getTargetLanguageKey(context: Context) =
        context
            .obtainLanguageSourceTargetDataStore
            .data
            .first()
            .asMap()[stringPreferencesKey(TARGET_LANGUAGE_KEY_PREF_KEY)] as String?

}