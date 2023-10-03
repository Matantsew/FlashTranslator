package com.example.latranslator.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.latranslator.DS_FRAME_BACKGROUND_COLOR
import com.example.latranslator.DS_FRAME_TEXT_COLOR
import com.example.latranslator.DS_TRANSLATION_FRAME_CORNERS_RADIUS
import com.example.latranslator.DS_TRANSLATION_FRAME_CURRENT_OFFSET_X
import com.example.latranslator.DS_TRANSLATION_FRAME_CURRENT_OFFSET_Y
import com.example.latranslator.DS_TRANSLATION_FRAME_TEXT_SIZE
import com.example.latranslator.utils.dataStoreMain
import kotlinx.coroutines.flow.first

object ParametersRepository {

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

    suspend fun setTranslationFrameCurrentOffsetX(context: Context, radius: Int) {
        context
            .dataStoreMain
            .edit { preferences ->
                preferences[intPreferencesKey(DS_TRANSLATION_FRAME_CURRENT_OFFSET_X)] = radius
            }
    }

    suspend fun getTranslationFrameCurrentOffsetX(context: Context) =
        context
            .dataStoreMain
            .data
            .first()
            .asMap()[intPreferencesKey(DS_TRANSLATION_FRAME_CURRENT_OFFSET_X)] as Int?

    suspend fun setTranslationFrameCurrentOffsetY(context: Context, radius: Int) {
        context
            .dataStoreMain
            .edit { preferences ->
                preferences[intPreferencesKey(DS_TRANSLATION_FRAME_CURRENT_OFFSET_Y)] = radius
            }
    }

    suspend fun getTranslationFrameCurrentOffsetY(context: Context) =
        context
            .dataStoreMain
            .data
            .first()
            .asMap()[intPreferencesKey(DS_TRANSLATION_FRAME_CURRENT_OFFSET_Y)] as Int?

    suspend fun setTranslationFrameTextSize(context: Context, textSize: Float) {
        context
            .dataStoreMain
            .edit { preferences ->
                preferences[floatPreferencesKey(DS_TRANSLATION_FRAME_TEXT_SIZE)] = textSize
            }
    }

    suspend fun getTranslationFrameTextSize(context: Context) =
        context
            .dataStoreMain
            .data
            .first()
            .asMap()[floatPreferencesKey(DS_TRANSLATION_FRAME_TEXT_SIZE)] as Float?

    suspend fun setFrameBackgroundColor(context: Context, color: Int) {
        context
            .dataStoreMain
            .edit { preferences ->
                preferences[intPreferencesKey(DS_FRAME_BACKGROUND_COLOR)] = color
            }
    }

    suspend fun getFrameBackgroundColor(context: Context) =
        context
            .dataStoreMain
            .data
            .first()
            .asMap()[intPreferencesKey(DS_FRAME_BACKGROUND_COLOR)] as Int?

    suspend fun setFrameTextColor(context: Context, color: Int) {
        context
            .dataStoreMain
            .edit { preferences ->
                preferences[intPreferencesKey(DS_FRAME_TEXT_COLOR)] = color
            }
    }

    suspend fun getFrameTextColor(context: Context) =
        context
            .dataStoreMain
            .data
            .first()
            .asMap()[intPreferencesKey(DS_FRAME_TEXT_COLOR)] as Int?
}