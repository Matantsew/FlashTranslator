package com.example.flashtranslator.data.data_source

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel

object LanguagesHelper {

    private val modelManager: RemoteModelManager by lazy { RemoteModelManager.getInstance() }

    fun isLanguageDownloaded(languageTag: String): Task<Boolean> {

        val model = TranslateRemoteModel.Builder(languageTag).build()

        return modelManager.isModelDownloaded(model)
    }

    fun obtainRemotelyLanguageModel(languageTag: String): Task<Void> {

        val model = TranslateRemoteModel.Builder(languageTag).build()

        val conditions = DownloadConditions.Builder()
            .build()

        return modelManager.download(model, conditions)
    }
}