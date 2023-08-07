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
                initAdapters(languagesList)
            }
        }

        spinnerSourceLanguage.onItemSelectedListener = this
        spinnerTargetLanguage.onItemSelectedListener = this

        return binding.root
    }

    private fun initAdapters(languages: List<Language>) {

        val spinnerSourceLanguageArrayAdapter: ArrayAdapter<Language> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages
        )

        spinnerSourceLanguageArrayAdapter.setDropDownViewResource(R.layout.spinner_language_dropdown_item)
        spinnerSourceLanguage.adapter = spinnerSourceLanguageArrayAdapter

        val spinnerTargetLanguageArrayAdapter: ArrayAdapter<Language> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages
        )

        spinnerTargetLanguageArrayAdapter.setDropDownViewResource(R.layout.spinner_language_dropdown_item)
        spinnerTargetLanguage.adapter = spinnerTargetLanguageArrayAdapter

        viewModel.sourceLanguagePosition.value?.let {
            spinnerSourceLanguage.setSelection(it)
        }

        viewModel.targetLanguagePosition.value?.let {
            spinnerTargetLanguage.setSelection(it)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>,
                                view: View?,
                                position: Int,
                                id: Long) {

        when(parent.id) {
            spinnerSourceLanguage.id -> {
                if(viewModel.sourceLanguagePosition.value != position) {
                    val languageKey = (parent.getItemAtPosition(position) as Language).key
                    viewModel.saveSourceLanguage(requireContext(), languageKey, position)
                }
            }
            spinnerTargetLanguage.id -> {
                if(viewModel.targetLanguagePosition.value != position) {
                    val languageKey = (parent.getItemAtPosition(position) as Language).key
                    viewModel.saveTargetLanguage(requireContext(), languageKey, position)
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