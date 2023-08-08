package com.example.latranslator.utils

import android.content.Context
import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.latranslator.DATA_STORE_SOURCE_TARGET_LANGUAGES

fun View.visible(v: Boolean) {
    visibility = if(v) View.VISIBLE else View.GONE
}

val Context.obtainLanguageSourceTargetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATA_STORE_SOURCE_TARGET_LANGUAGES
)

fun String.convertLanguageKeyToName() =
    when(this) {
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

        else -> this
    }
