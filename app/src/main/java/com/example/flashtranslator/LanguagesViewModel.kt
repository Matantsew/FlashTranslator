package com.example.flashtranslator

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashtranslator.data.repositories.LanguagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject internal constructor(@ApplicationContext context: Context,
                                                      private val languagesRepository: LanguagesRepository)
    : ViewModel() {

    private var _availableLanguages = MutableLiveData<List<Language>>(listOf())
    val availableLanguages: LiveData<List<Language>> get() = _availableLanguages

    private var _downloadedLanguages = MutableLiveData<List<Language>>(listOf())
    val downloadedLanguages: LiveData<List<Language>> get() = _downloadedLanguages

    private var _downloadingLanguagesKeysSet = MutableStateFlow<HashSet<String>>(hashSetOf())
    val downloadingLanguagesKeysSet: StateFlow<HashSet<String>> = _downloadingLanguagesKeysSet

    private var _sourceLanguagePosition = MutableLiveData<Int?>()
    val sourceLanguagePosition: LiveData<Int?> get() = _sourceLanguagePosition

    private var _targetLanguagePosition = MutableLiveData<Int?>()
    val targetLanguagePosition: LiveData<Int?> get() = _targetLanguagePosition

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

            val sourcePosition = languagesRepository.getSourceLanguagePosition(context)
            _sourceLanguagePosition.postValue(sourcePosition)
        }

        viewModelScope.launch(Dispatchers.Main) {
            val targetPosition = languagesRepository.getTargetLanguagePosition(context)
            _targetLanguagePosition.postValue(targetPosition)
        }
    }

    fun obtainDownloadedLanguages() {
        viewModelScope.launch(Dispatchers.Main) {
            languagesRepository.getDownloadedLanguages { languagesList ->
                _downloadedLanguages.value = languagesList
            }
        }
    }

    fun obtainOrWaitLanguageModelRemotely(languageTag: String, onCompleteBlock: (complete: Boolean) -> Unit) {

        _downloadingLanguagesKeysSet.value.add(languageTag)

        languagesRepository.downloadLanguageModel(languageTag).addOnCompleteListener {
            _downloadingLanguagesKeysSet.value.remove(languageTag)
            onCompleteBlock(it.isSuccessful)
        }
    }

    fun saveSourceLanguage(context: Context, key: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            languagesRepository.setSourceLanguagePosition(context, position)
            languagesRepository.setSourceLanguageKey(context, key)
            _sourceLanguagePosition.postValue(position)
        }
    }

    fun saveTargetLanguage(context: Context, key: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            languagesRepository.setTargetLanguagePosition(context, position)
            languagesRepository.setTargetLanguageKey(context, key)
            _targetLanguagePosition.postValue(position)
        }
    }
}