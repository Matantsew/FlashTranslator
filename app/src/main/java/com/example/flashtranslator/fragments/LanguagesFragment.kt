package com.example.flashtranslator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashtranslator.*
import com.example.flashtranslator.adapters.LanguagesListAdapter
import com.example.flashtranslator.databinding.FragmentLanguagesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguagesFragment : Fragment() {

    private var _binding: FragmentLanguagesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralViewModel by activityViewModels()

    private var languagesAdapter: LanguagesListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        languagesAdapter = LanguagesListAdapter(viewModel)

        lifecycleScope.launchWhenCreated {
            viewModel.availableLanguages.observe(requireActivity()) {
                launch(Dispatchers.Main) {
                    languagesAdapter?.setLanguages(it)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentLanguagesBinding.inflate(layoutInflater)

        with(binding.listViewLanguages) {
            layoutManager = LinearLayoutManager(context)
            adapter = languagesAdapter
        }

        return binding.root
    }
}