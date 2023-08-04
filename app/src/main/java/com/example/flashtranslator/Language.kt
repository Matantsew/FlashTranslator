package com.example.flashtranslator

import com.example.flashtranslator.data.data_source.LanguagesHelper

data class Language(var key: String,
                    var name: String? = null,
                    var isDownloaded: Boolean? = null) {

    init {
        prepare()
        name = convertLanguageKeyToName(key)
    }

    private fun prepare() = LanguagesHelper.isLanguageModelDownloaded(key).addOnSuccessListener {
        isDownloaded = it
    }

    private fun convertLanguageKeyToName(key: String): String {

        return when(key){
            "af" -> "AFRIKAANS"
            "sq" -> "ALBANIAN"
            "ar" -> "ARABIC"

            "be" -> "BELARUSIAN"
            "bg" -> "BULGARIAN"
            "bn" -> "BENGALI"

            "ca" -> "CATALAN"
            "zh" -> "CHINESE"
            "hr" -> "CROATIAN"
            "cs" -> "CZECH"

            "da" -> "DANISH"
            "nl" -> "DUTCH"

            "en" -> "ENGLISH"
            "eo" -> "ESPERANTO"
            "et" -> "ESTONIAN"

            "fi" -> "FINNISH"
            "fr" -> "FRENCH"

            "gl" -> "GALICIAN"
            "ka" -> "GEORGIAN"
            "de" -> "GERMAN"
            "el" -> "GREEK"
            "gu" -> "GUJARATI"

            "ht" -> "HAITIAN CREOLE"
            "he" -> "HEBREW"
            "hi" -> "HINDI"
            "hu" -> "HUNGARIAN"

            "is" -> "ICELANDIC"
            "id" -> "INDONESIAN"
            "ga" -> "IRISH"
            "it" -> "ITALIAN"

            "ja" -> "JAPANESE"

            "kn" -> "KANNADA"
            "ko" -> "KOREAN"

            "lt" -> "LITHUANIAN"
            "lv" -> "LATVIAN"

            "mk" -> "MACEDONIAN"
            "mr" -> "MARATHI"
            "ms" -> "MALAY"
            "mt" -> "MALTESE"

            "no" -> "NORWEGIAN"

            "fa" -> "PERSIAN"
            "pl" -> "POLISH"
            "pt" -> "PORTUGUESE"

            "ro" -> "ROMANIAN"
            "ru" -> "RUSSIAN"

            "sk" -> "SLOVAK"
            "sl" -> "SLOVENIAN"
            "es" -> "SPANISH"
            "sv" -> "SWEDISH"
            "sw" -> "SWAHILI"

            "tl" -> "TAGALOG"
            "ta" -> "TAMIL"
            "te" -> "TELUGU"
            "th" -> "THAI"
            "tr" -> "TURKISH"

            "uk" -> "UKRAINIAN"
            "ur" -> "URDU"

            "vi" -> "VIETNAMESE"

            "cy" -> "WELSH"

            else -> key
        }
    }
}