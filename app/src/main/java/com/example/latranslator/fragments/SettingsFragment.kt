package com.example.latranslator.fragments

import android.content.res.ColorStateList
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
import com.example.latranslator.utils.customAmbilWarnaColorPicker.CustomAmbilWarnaDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val mainScope = CoroutineScope(Dispatchers.Main)

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater)

        viewModel.obtainTranslationFrameCornerRadius(requireContext())
        viewModel.obtainTranslationFrameTextSize(requireContext())

        binding.seekBarRadius.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progressRadius: Int, b: Boolean) {
                refreshPreview(cornersRadius = progressRadius.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val radius = seekBar.progress.toFloat()
                viewModel.setTranslationFrameCornerRadius(requireContext(), radius)
            }
        })

        binding.seekBarTextSize.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progressTextSize: Int, b: Boolean) {
                refreshPreview(textSize = progressTextSize.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val textSize = seekBar.progress.toFloat()
                viewModel.setTranslationFrameTextSize(requireContext(), textSize)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            viewModel.frameCornersRadius.collect {
                binding.seekBarRadius.progress = it.toInt()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.frameTextSize.collect {
                binding.seekBarTextSize.progress = it.toInt()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.frameBackgroundColor.collect { color ->
                binding.buttonBackgroundColor.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.frameTextColor.collect { color ->
                binding.buttonFrameTextColor.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        binding.buttonBackgroundColor.setOnClickListener {
            CustomAmbilWarnaDialog(requireContext(),
                viewModel.frameBackgroundColor.value,
                object : OnAmbilWarnaListener {
                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                        viewModel.setFrameBackgroundColor(requireContext(), color)
                    }
                    override fun onCancel(dialog: AmbilWarnaDialog?) {
                    }
                }).show()
        }

        binding.buttonFrameTextColor.setOnClickListener {
            CustomAmbilWarnaDialog(requireContext(),
                viewModel.frameTextColor.value,
                object : OnAmbilWarnaListener {
                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                        viewModel.setFrameTextColor(requireContext(), color)
                    }
                    override fun onCancel(dialog: AmbilWarnaDialog?) {
                    }
                }).show()
        }
    }

    private fun refreshPreview(
        cornersRadius: Float? = null,
        textSize: Float? = null
    ) {
        mainScope.launch {
            viewModel.obtainTranslationFrameCornerRadius(requireContext())
            viewModel.obtainTranslationFrameTextSize(requireContext())

            val r = cornersRadius ?: async { viewModel.frameCornersRadius.value }.await()
            val s = textSize ?: async { viewModel.frameTextSize.value }.await()

            val shape = ShapeDrawable(
                RoundRectShape(
                    floatArrayOf(
                        r,
                        r,
                        r,
                        r,
                        r,
                        r,
                        r,
                        r
                    ), null, null
                )
            )

            shape.paint.color = Color.LTGRAY
            binding.translationFrame.textTranslation.textSize = s
            binding.translationFrame.root.background = shape
        }
    }
}