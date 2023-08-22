package com.example.latranslator.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.latranslator.DS_SOURCE_LANGUAGE_KEY_PREF_KEY
import com.example.latranslator.DS_TARGET_LANGUAGE_KEY_PREF_KEY
import com.example.latranslator.DS_TRANSLATION_FRAME_CORNERS_RADIUS
import com.example.latranslator.data.Language
import com.example.latranslator.data.data_source.LanguagesHelper
import com.example.latranslator.utils.dataStoreMain
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

object LanguagesRepository {

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

    fun downloadLanguageModel(languageKey: String) = LanguagesHelper.downloadRemotelyLanguageModel(languageKey)
    fun deleteLanguageModel(languageKey: String) = LanguagesHelper.deleteRemotelyLanguageModel(languageKey)

    suspend fun setSourceLanguageKey(context: Context, key: String) {
        context
            .dataStoreMain
            .edit { preferences ->
                preferences[stringPreferencesKey(DS_SOURCE_LANGUAGE_KEY_PREF_KEY)] = key
            }
    }

    suspend fun setTargetLanguageKey(context: Context, key: String) {
        context
            .dataStoreMain
            .edit { preferences ->
                preferences[stringPreferencesKey(DS_TARGET_LANGUAGE_KEY_PREF_KEY)] = key
            }
    }

    suspend fun getSourceLanguageKey(context: Context) =
        context
            .dataStoreMain
            .data
            .first()
            .asMap()[stringPreferencesKey(DS_SOURCE_LANGUAGE_KEY_PREF_KEY)] as String?

    suspend fun getTargetLanguageKey(context: Context) =
        context
            .dataStoreMain
            .data
            .first()
            .asMap()[stringPreferencesKey(DS_TARGET_LANGUAGE_KEY_PREF_KEY)] as String?

    suspend fun setTranslationFrameCornerRadius(context: Context, radius: Float) {
        context
            .dataStoreMain
            .edit { preferences ->
                preferences[floatPreferencesKey(DS_TRANSLATION_FRAME_CORNERS_RADIUS)] = radius
            }
    }

    suspend fun getTranslationFrameCornerRadius(context: Context) =
        context
            .dataStoreMain
            .data
            .first()
            .asMap()[floatPreferencesKey(DS_TRANSLATION_FRAME_CORNERS_RADIUS)] as Float?
}