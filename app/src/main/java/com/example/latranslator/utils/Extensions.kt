package com.example.latranslator.utils

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.latranslator.DATA_STORE_MAIN
import com.example.latranslator.services.TranslateAccessibilityService

fun Context.isAccessibilityTurnedOn(): Boolean {

    val prefString = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )

    return prefString != null && prefString.contains(packageName + "/" + TranslateAccessibilityService::class.java.name)
}

fun View.visible(v: Boolean) {
    visibility = if(v) View.VISIBLE else View.GONE
}

val Context.dataStoreMain: DataStore<Preferences> by preferencesDataStore(
    name = DATA_STORE_MAIN
)

fun createLayoutParameters(x: Int, y: Int): WindowManager.LayoutParams {

    val type = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    else WindowManager.LayoutParams.TYPE_PHONE

    val params = WindowManager.LayoutParams(
        0,
        0,
        type,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT)

    params.x = x
    params.y = y

    params.width = WindowManager.LayoutParams.WRAP_CONTENT
    params.height = WindowManager.LayoutParams.WRAP_CONTENT

    return params
}

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
