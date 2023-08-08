package com.example.latranslator.data.data_source

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

object LanguagesHelper {

    private val modelManager: RemoteModelManager by lazy { RemoteModelManager.getInstance() }

    fun getDownloadedLanguagesModels(): Task<MutableSet<TranslateRemoteModel>>
        = modelManager.getDownloadedModels(TranslateRemoteModel::class.java)

    fun isLanguageModelDownloaded(languageKey: String): Task<Boolean> {
        val model = buildTranslateRemoteModelByLanguageKey(languageKey)
        return modelManager.isModelDownloaded(model)
    }

    fun downloadRemotelyLanguageModel(languageKey: String): Task<Void> {

        val model = buildTranslateRemoteModelByLanguageKey(languageKey)

        val conditions = DownloadConditions.Builder()
            .build()

        return modelManager.download(model, conditions)
    }

    fun deleteRemotelyLanguageModel(languageKey: String) : Task<Void> {

        val model = buildTranslateRemoteModelByLanguageKey(languageKey)

        return modelManager.deleteDownloadedModel(model)
    }

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

    private fun buildTranslateRemoteModelByLanguageKey(languageKey: String) = TranslateRemoteModel.Builder(languageKey).build()
}