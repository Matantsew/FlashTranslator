package com.example.latranslator

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.latranslator.data.data_source.LanguagesHelper
import com.example.latranslator.data.repositories.LanguagesRepository
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
class GeneralViewModel @Inject internal constructor(@ApplicationContext context: Context,
                                                    private val languagesRepository: LanguagesRepository)
    : ViewModel() {

    private var _accessibilityTurnedOn = MutableStateFlow(false)
    val accessibilityTurnedOn: StateFlow<Boolean> = _accessibilityTurnedOn

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

    init {

        viewModelScope.launch(Dispatchers.IO) {
            val languagesList = mutableListOf<Language>()
            languagesRepository.getAvailableLanguages().collect { language ->
                languagesList.add(language)
            }

            withContext(Dispatchers.Main) {
                _availableLanguages.value = languagesList
            }
        }

        obtainDownloadedLanguages()

        viewModelScope.launch(Dispatchers.Main) {

            val sourcePosition = languagesRepository.getSourceLanguageKey(context)
            _sourceLanguageKey.postValue(sourcePosition)
        }

        viewModelScope.launch(Dispatchers.Main) {
            val targetPosition = languagesRepository.getTargetLanguageKey(context)
            _targetLanguageKey.postValue(targetPosition)
        }

        checkAccessibilityTurnedOn(context)
    }

    fun checkAccessibilityTurnedOn(context: Context) {
        viewModelScope.launch {
            _accessibilityTurnedOn.value = isAccessibilityTurnedOn(context, TranslateAccessibilityService::class.java)
        }
    }

    fun showAccessibilityAlertDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.accessibility_check_title)
            .setMessage(
                R.string.accessibility_opening
            )
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok,
            ) { dialog, which -> openAccessibilitySettings(context) }
            .show()
    }

    fun openAccessibilitySettings(context: Context) {
        val accessibilitySettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(accessibilitySettingsIntent)
    }

    fun obtainDownloadedLanguages() {
        viewModelScope.launch(Dispatchers.Main) {
            languagesRepository.getDownloadedLanguages { languagesList ->
                _downloadedLanguages.value = languagesList
            }
        }
    }

    fun obtainOrWaitLanguageModelRemotely(languageKey: String, onCompleteBlock: (complete: Boolean) -> Unit) {

        _processingLanguagesKeysSet.value.add(languageKey)

        languagesRepository.downloadLanguageModel(languageKey).addOnCompleteListener {
            _processingLanguagesKeysSet.value.remove(languageKey)
            onCompleteBlock(it.isSuccessful)
        }
    }

    fun deleteLanguageModel(languageKey: String, onCompleteBlock: (complete: Boolean) -> Unit) {
        _processingLanguagesKeysSet.value.add(languageKey)

        languagesRepository.deleteLanguageModel(languageKey).addOnCompleteListener {
            _processingLanguagesKeysSet.value.remove(languageKey)
            onCompleteBlock(it.isSuccessful)
        }
    }

    fun setFromLanguage(context: Context, key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            languagesRepository.setSourceLanguageKey(context, key)
            _sourceLanguageKey.postValue(key)
        }
    }

    fun setToLanguage(context: Context, key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            languagesRepository.setTargetLanguageKey(context, key)
            _targetLanguageKey.postValue(key)
        }
    }

    fun refreshLanguage(languageKey: String) {
        LanguagesHelper.isLanguageModelDownloaded(languageKey).addOnSuccessListener { isDownLoaded ->
            _availableLanguages.value?.find {
                it.key == languageKey
            }?.isDownloaded = isDownLoaded
        }
    }
}