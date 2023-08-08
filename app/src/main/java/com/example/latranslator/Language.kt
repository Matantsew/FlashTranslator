package com.example.latranslator

import com.example.latranslator.data.data_source.LanguagesHelper
import com.example.latranslator.utils.convertLanguageKeyToName

data class Language(var key: String,
                    var isDownloaded: Boolean? = null) {

    init {
        prepare()
    }

    private fun prepare() = LanguagesHelper.isLanguageModelDownloaded(key).addOnSuccessListener {
        isDownloaded = it
    }

    override fun toString(): String {
        return key.convertLanguageKeyToName()
    }
}