package com.example.flashtranslator

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashtranslator.data.repositories.LanguagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject internal constructor(@ApplicationContext context: Context,
                                                      private val languagesRepository: LanguagesRepository)
    : ViewModel() {

    private var _availableLanguages = MutableLiveData<List<Language>>(listOf())
    val availableLanguages: LiveData<List<Language>> get() = _availableLanguages

    private var _downloadingLanguagesKeysSet = MutableStateFlow<HashSet<String>>(hashSetOf())
    val downloadingLanguagesKeysSet: StateFlow<HashSet<String>> = _downloadingLanguagesKeysSet

    init {
        viewModelScope.launch {
            val languagesList = mutableListOf<Language>()
            languagesRepository.getAvailableLanguages().collect {
                languagesList.add(it)
            }

            _availableLanguages.value = languagesList
        }
    }

    fun obtainLanguageModelRemotely(languageTag: String, onCompleteBlock: (complete: Boolean) -> Unit) {

        _downloadingLanguagesKeysSet.value.add(languageTag)

        languagesRepository.downloadLanguageModel(languageTag).addOnCompleteListener {
            _downloadingLanguagesKeysSet.value.remove(languageTag)
            onCompleteBlock(it.isSuccessful)
        }
    }
}