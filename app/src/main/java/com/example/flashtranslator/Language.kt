package com.example.flashtranslator

import com.example.flashtranslator.data.data_source.LanguagesHelper
import com.example.flashtranslator.utils.convertLanguageKeyToName

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