package com.example.flashtranslator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.datastore.preferences.preferencesKey
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashtranslator.*
import com.example.flashtranslator.data.data_source.DataStoreHelper
import com.example.flashtranslator.databinding.FragmentTranslatorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TranslatorFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentTranslatorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LanguagesViewModel by activityViewModels()

    private lateinit var spinnerSourceLanguage: Spinner
    private lateinit var spinnerTargetLanguage: Spinner
    private lateinit var languagesTags: MutableList<String>
    private lateinit var languages: MutableList<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentTranslatorBinding.inflate(layoutInflater)

        spinnerSourceLanguage = binding.languageSelectorInclude.fromLanguage
        spinnerTargetLanguage = binding.languageSelectorInclude.toLanguage

        val languagesDataStore = DataStoreHelper.obtainLanguagesDatastore(requireContext())
        val languageSourceTargetPositionsDS = DataStoreHelper.obtainLanguageSourceTargetDataStore(requireContext())

        lifecycleScope.launch {

            val prefLanguages = languagesDataStore.data.first()
            val positions = languageSourceTargetPositionsDS.data.first()

            languagesTags = mutableListOf()
            languages = mutableListOf()

            prefLanguages.asMap().forEach {
                languagesTags.add(it.key.name)
                languages.add(it.value.toString())
            }

            val spinnerFromLanguageArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                languages
            )

            spinnerFromLanguageArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSourceLanguage.adapter = spinnerFromLanguageArrayAdapter

            val spinnerToLanguageArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                languages
            )

            spinnerToLanguageArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTargetLanguage.adapter = spinnerToLanguageArrayAdapter

            if(positions.asMap().isNotEmpty()){

                val sourcePosition = positions.asMap().getValue(preferencesKey<Int>(
                    SOURCE_POSITION
                )) as Int

                val targetPosition = positions.asMap().getValue(preferencesKey<Int>(
                    TARGET_POSITION
                )) as Int

                spinnerSourceLanguage.setSelection(sourcePosition)
                spinnerTargetLanguage.setSelection(targetPosition)
            }
        }

        spinnerSourceLanguage.onItemSelectedListener = this
        spinnerTargetLanguage.onItemSelectedListener = this

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

        when(parent.id){
            spinnerSourceLanguage.id -> saveLanguagesPosition(SOURCE_POSITION, SOURCE_LANGUAGE, position)
            spinnerTargetLanguage.id -> saveLanguagesPosition(TARGET_POSITION, TARGET_LANGUAGE, position)

        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun saveLanguagesPosition(positionPreferenceTag: String, languagePreferenceTag: String, position: Int){

        val language = languagesTags[position]
/*

        LanguagesRepository.saveSourceTargetLanguages(scope, languageSourceTargetPositionsDS, positionPreferenceTag, position)
        LanguagesRepository.saveSourceTargetLanguages(scope, languageSourceTargetPositionsDS, languagePreferenceTag, language)
*/

    }
}