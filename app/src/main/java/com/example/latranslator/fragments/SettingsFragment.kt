package com.example.latranslator.fragments

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.latranslator.GeneralViewModel
import com.example.latranslator.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater)

        viewModel.obtainTranslationFrameCornerRadius(requireContext())

        binding.seekBarRadius.progress = viewModel.frameCornersRadius.value.toInt()

        lifecycleScope.launchWhenCreated {
            viewModel.frameCornersRadius.collect {
                binding.seekBarRadius.progress = it.toInt()
            }
        }

        binding.seekBarRadius.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progressRadius: Int, b: Boolean) {
                refreshPreview(progressRadius.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val radius = seekBar.progress.toFloat()
                viewModel.setTranslationFrameCornerRadius(requireContext(), radius)
            }
        })

        return binding.root
    }

    private fun refreshPreview(progressRadius: Float) {

        val shape = ShapeDrawable(
            RoundRectShape(
                floatArrayOf(
                    progressRadius,
                    progressRadius,
                    progressRadius,
                    progressRadius,
                    progressRadius,
                    progressRadius,
                    progressRadius,
                    progressRadius
                ), null, null
            )
        )

        shape.paint.color = Color.LTGRAY
        binding.translationFrame.root.background = shape
    }
}