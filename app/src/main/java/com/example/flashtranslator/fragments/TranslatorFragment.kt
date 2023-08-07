package com.example.flashtranslator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.flashtranslator.*
import com.example.flashtranslator.databinding.FragmentTranslatorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TranslatorFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentTranslatorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LanguagesViewModel by activityViewModels()

    private lateinit var spinnerSourceLanguage: Spinner
    private lateinit var spinnerTargetLanguage: Spinner

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentTranslatorBinding.inflate(layoutInflater)

        spinnerSourceLanguage = binding.fromLanguage
        spinnerTargetLanguage = binding.toLanguage

        lifecycleScope.launch(Dispatchers.Main) {

            viewModel.downloadedLanguages.observe(viewLifecycleOwner) { languagesList ->
                languagesList.sortedBy { language ->
                    language.key
                }
                initAdapters(languagesList)
            }
        }

        spinnerSourceLanguage.onItemSelectedListener = this
        spinnerTargetLanguage.onItemSelectedListener = this

        return binding.root
    }

    private fun initAdapters(languagesKeys: List<Language>) {

        val spinnerSourceLanguageArrayAdapter: ArrayAdapter<Language> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languagesKeys
        )

        spinnerSourceLanguageArrayAdapter.setDropDownViewResource(R.layout.spinner_language_dropdown_item)
        spinnerSourceLanguage.adapter = spinnerSourceLanguageArrayAdapter

        val spinnerTargetLanguageArrayAdapter: ArrayAdapter<Language> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languagesKeys
        )

        spinnerTargetLanguageArrayAdapter.setDropDownViewResource(R.layout.spinner_language_dropdown_item)
        spinnerTargetLanguage.adapter = spinnerTargetLanguageArrayAdapter

        viewModel.sourceLanguageKey.value?.let { key ->
            val selectedLanguageIndex = languagesKeys.indexOfFirst { it.key == key}
            spinnerSourceLanguage.setSelection(selectedLanguageIndex)
        }

        viewModel.targetLanguageKey.value?.let { key ->
            val selectedLanguageIndex = languagesKeys.indexOfFirst { it.key == key}
            spinnerTargetLanguage.setSelection(selectedLanguageIndex)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>,
                                view: View?,
                                position: Int,
                                id: Long) {

        when(parent.id) {
            spinnerSourceLanguage.id -> {
                val language = viewModel.downloadedLanguages.value?.get(position)
                if(viewModel.sourceLanguageKey.value != language?.key) {
                    val languageKey = (parent.getItemAtPosition(position)) as Language
                    viewModel.saveSourceLanguage(requireContext(), languageKey.key)
                }
            }
            spinnerTargetLanguage.id -> {
                val language = viewModel.downloadedLanguages.value?.get(position)
                if(viewModel.targetLanguageKey.value != language?.key) {
                    val languageKey = (parent.getItemAtPosition(position)) as Language
                    viewModel.saveTargetLanguage(requireContext(), languageKey.key)
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}