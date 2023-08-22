package com.example.latranslator.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.example.latranslator.DS_TRANSLATION_FRAME_CORNERS_RADIUS
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
}