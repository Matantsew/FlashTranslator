package com.example.latranslator

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.latranslator.data.Language
import com.example.latranslator.data.data_source.LanguagesHelper
import com.example.latranslator.data.repositories.LanguagesRepository
import com.example.latranslator.data.repositories.ParametersRepository
import com.example.latranslator.utils.isAccessibilityTurnedOn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GeneralViewModel @Inject internal constructor(@ApplicationContext context: Context)
    : ViewModel() {

    // Languages:
    private var _availableLanguages = MutableLiveData<List<Language>>(listOf())
    val availableLanguages: LiveData<List<Language>> get() = _availableLanguages

    private var _downloadedLanguages = MutableLiveData<List<Language>>(listOf())
    val downloadedLanguages: LiveData<List<Language>> get() = _downloadedLanguages

    private var _processingLanguagesKeysSet = MutableStateFlow<HashSet<String>>(hashSetOf())
    val processingLanguagesKeysSet: StateFlow<HashSet<String>> = _processingLanguagesKeysSet

    private var _sourceLanguageKey = MutableLiveData<String?>()
    val fromLanguageKey: LiveData<String?> get() = _sourceLanguageKey

    private var _targetLanguageKey = MutableLiveData<String?>()
    val toLanguageKey: LiveData<String?> get() = _targetLanguageKey

    // Frame parameters (UI):
    private var _frameCornersRadius = MutableStateFlow(0f)
    val frameCornersRadius: StateFlow<Float> = _frameCornersRadius

    private var _frameTextSize = MutableStateFlow(0f)
    val frameTextSize: StateFlow<Float> = _frameTextSize

    private var _frameBackgroundColor = MutableStateFlow(0)
    val frameBackgroundColor: StateFlow<Int> = _frameBackgroundColor

    private var _frameTextColor = MutableStateFlow(0)
    val frameTextColor: StateFlow<Int> = _frameTextColor

    // Other:
    private var _accessibilityTurnedOn = MutableStateFlow(false)
    val accessibilityTurnedOn: StateFlow<Boolean> = _accessibilityTurnedOn

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val languagesList = mutableListOf<Language>()
            LanguagesRepository.getAvailableLanguages().collect { language ->
                languagesList.add(language)
            }

            withContext(Dispatchers.Main) {
                _availableLanguages.value = languagesList
            }
        }

        obtainDownloadedLanguages()

        viewModelScope.launch(Dispatchers.Main) {
            val sourcePosition = LanguagesRepository.getSourceLanguageKey(context)
            _sourceLanguageKey.postValue(sourcePosition)
        }

        viewModelScope.launch(Dispatchers.Main) {
            val targetPosition = LanguagesRepository.getTargetLanguageKey(context)
            _targetLanguageKey.postValue(targetPosition)
        }

        viewModelScope.launch(Dispatchers.Main) {
            val color = ParametersRepository.getFrameBackgroundColor(context) ?: -4203791
            _frameBackgroundColor.value = color
        }

        viewModelScope.launch(Dispatchers.Main) {
            val color = ParametersRepository.getFrameTextColor(context) ?: -12961222
            _frameTextColor.value = color
        }

        checkAccessibilityTurnedOn(context)
    }

    fun checkAccessibilityTurnedOn(context: Context) {
        viewModelScope.launch {
            _accessibilityTurnedOn.value = context.isAccessibilityTurnedOn()
        }
    }

    fun showAccessibilityAlertDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.accessibility_check_dialog_title)
            .setMessage(
                R.string.accessibility_opening_dialog_description
            )
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok,
            ) { _, _ -> openAccessibilitySettings(context) }
            .show()
    }

    fun openAccessibilitySettings(context: Context) {
        val accessibilitySettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(accessibilitySettingsIntent)
    }

    fun obtainDownloadedLanguages() {
        viewModelScope.launch(Dispatchers.Main) {
            LanguagesRepository.getDownloadedLanguages { languagesList ->
                _downloadedLanguages.value = languagesList
            }
        }
    }

    fun obtainOrWaitLanguageModelRemotely(languageKey: String, onCompleteBlock: (complete: Boolean) -> Unit) {
        _processingLanguagesKeysSet.value.add(languageKey)

        LanguagesRepository.downloadLanguageModel(languageKey).addOnCompleteListener {
            _processingLanguagesKeysSet.value.remove(languageKey)
            onCompleteBlock(it.isSuccessful)
        }
    }

    fun deleteLanguageModel(languageKey: String, onCompleteBlock: (complete: Boolean) -> Unit) {
        _processingLanguagesKeysSet.value.add(languageKey)

        LanguagesRepository.deleteLanguageModel(languageKey).addOnCompleteListener {
            _processingLanguagesKeysSet.value.remove(languageKey)
            onCompleteBlock(it.isSuccessful)
        }
    }

    fun setFromLanguage(context: Context, key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            LanguagesRepository.setSourceLanguageKey(context, key)
            _sourceLanguageKey.postValue(key)
        }
    }

    fun setToLanguage(context: Context, key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            LanguagesRepository.setTargetLanguageKey(context, key)
            _targetLanguageKey.postValue(key)
        }
    }

    fun refreshLanguage(languageKey: String) {
        LanguagesHelper.isLanguageModelDownloaded(languageKey).addOnSuccessListener { isDownloaded ->
            _availableLanguages.value?.find {
                it.key == languageKey
            }?.isDownloaded = isDownloaded
        }
    }

    fun setTranslationFrameCornerRadius(context: Context, radius: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            ParametersRepository.setTranslationFrameCornerRadius(context, radius)
        }
    }

    fun obtainTranslationFrameCornerRadius(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            _frameCornersRadius.value = ParametersRepository.getTranslationFrameCornerRadius(context) ?: 0f
        }
    }

    fun setTranslationFrameTextSize(context: Context, textSize: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            ParametersRepository.setTranslationFrameTextSize(context, textSize)
        }
    }

    fun obtainTranslationFrameTextSize(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            _frameTextSize.value = ParametersRepository.getTranslationFrameTextSize(context) ?: 24.0f
        }
    }

    fun setFrameBackgroundColor(context: Context, color: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            ParametersRepository.setFrameBackgroundColor(context, color)
            withContext(Dispatchers.Main) {
                _frameBackgroundColor.value = color
            }
        }
    }

    fun setFrameTextColor(context: Context, color: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            ParametersRepository.setFrameTextColor(context, color)
            withContext(Dispatchers.Main) {
                _frameTextColor.value = color
            }
        }
    }
}