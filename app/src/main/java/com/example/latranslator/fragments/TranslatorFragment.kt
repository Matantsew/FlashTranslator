package com.example.latranslator.fragments

import android.content.res.ColorStateList
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
import com.example.latranslator.GeneralViewModel
import com.example.latranslator.Language
import com.example.latranslator.R
import com.example.latranslator.databinding.FragmentTranslatorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TranslatorFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentTranslatorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralViewModel by activityViewModels()

    private lateinit var fromLanguageSpinner: Spinner
    private lateinit var toLanguageSpinner: Spinner

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentTranslatorBinding.inflate(inflater)

        fromLanguageSpinner = binding.fromLanguageSpinner
        toLanguageSpinner = binding.toLanguageSpinner

        viewModel.downloadedLanguages.observe(viewLifecycleOwner) { languagesList ->
            languagesList.sortedBy { language ->
                language.key
            }
            initAdapters(languagesList)
        }

        lifecycleScope.launchWhenCreated {
            viewModel.accessibilityTurnedOn.collect { turnedOn ->
                if (turnedOn) {
                    binding.openAccessibilityButton.text = requireContext().getText(R.string.turn_off)
                    binding.openAccessibilityButton.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.gray))
                }
                else {
                    binding.openAccessibilityButton.text = requireContext().getText(R.string.turn_on)
                    binding.openAccessibilityButton.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.gray_sea))
                }
            }
        }

        binding.openAccessibilityButton.setOnClickListener {
            if(!viewModel.accessibilityTurnedOn.value) {
                viewModel.showAccessibilityAlertDialog(requireContext())
            }
            else viewModel.openAccessibilitySettings(requireContext())
        }

        binding.swap.setOnClickListener {
            val fromPosition = fromLanguageSpinner.selectedItemPosition
            val toPosition = toLanguageSpinner.selectedItemPosition

            fromLanguageSpinner.setSelection(toPosition)
            toLanguageSpinner.setSelection(fromPosition)
        }

        fromLanguageSpinner.onItemSelectedListener = this
        toLanguageSpinner.onItemSelectedListener = this

        return binding.root
    }

    private fun initAdapters(languagesKeys: List<Language>) {

        val spinnerSourceLanguageArrayAdapter: ArrayAdapter<Language> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languagesKeys
        )

        spinnerSourceLanguageArrayAdapter.setDropDownViewResource(R.layout.spinner_language_dropdown_item)
        fromLanguageSpinner.adapter = spinnerSourceLanguageArrayAdapter

        val spinnerTargetLanguageArrayAdapter: ArrayAdapter<Language> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languagesKeys
        )

        spinnerTargetLanguageArrayAdapter.setDropDownViewResource(R.layout.spinner_language_dropdown_item)
        toLanguageSpinner.adapter = spinnerTargetLanguageArrayAdapter

        viewModel.fromLanguageKey.value?.let { key ->
            val selectedLanguageIndex = languagesKeys.indexOfFirst { it.key == key}
            fromLanguageSpinner.setSelection(selectedLanguageIndex)
        }

        viewModel.toLanguageKey.value?.let { key ->
            val selectedLanguageIndex = languagesKeys.indexOfFirst { it.key == key}
            toLanguageSpinner.setSelection(selectedLanguageIndex)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>,
                                view: View?,
                                position: Int,
                                id: Long) {

        when(parent.id) {
            fromLanguageSpinner.id -> {
                val language = viewModel.downloadedLanguages.value?.get(position)
                if(viewModel.fromLanguageKey.value != language?.key) {
                    val languageKey = (parent.getItemAtPosition(position)) as Language
                    viewModel.setFromLanguage(requireContext(), languageKey.key)
                }
            }
            toLanguageSpinner.id -> {
                val language = viewModel.downloadedLanguages.value?.get(position)
                if(viewModel.toLanguageKey.value != language?.key) {
                    val languageKey = (parent.getItemAtPosition(position)) as Language
                    viewModel.setToLanguage(requireContext(), languageKey.key)
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