package com.example.flashtranslator.data.repositories

import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import com.example.flashtranslator.Language
import com.example.flashtranslator.data.data_source.LanguagesHelper
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagesRepository @Inject constructor() {

    suspend fun saveSourceTargetLanguages(positionsSourceTargetDS: DataStore<Preferences>,
                                          key: String,
                                          position: Int) {

        val dataStoreKey = preferencesKey<Int>(key)

        positionsSourceTargetDS.edit { positions ->
            positions[dataStoreKey] = position
        }

        Log.i("SAVED_POSITION", position.toString())
    }

    suspend fun saveSourceTargetLanguages(positionsSourceTargetDS: DataStore<Preferences>,
                                          key: String,
                                          language: String) {

            val dataStoreKey = preferencesKey<String>(key)

            positionsSourceTargetDS.edit { positions ->
                positions[dataStoreKey] = language
            }

        Log.i("SAVED_LANGUAGE", language)
    }

    suspend fun getAvailableLanguages() = flow {

        val languageKeys = TranslateLanguage.getAllLanguages()

        languageKeys.forEach { key ->
            emit((Language(key)))
        }
    }

    fun downloadLanguageModel(languageTag: String) = LanguagesHelper.obtainRemotelyLanguageModel(languageTag)

    fun getTranslatorClient(sourceLanguage: String, targetLanguage: String): Translator {

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        Translation.getClient(options).downloadModelIfNeeded()
            .addOnSuccessListener {
                Log.i("TRANSLATOR", "Downloaded!")
            }

        return Translation.getClient(options)
    }
}