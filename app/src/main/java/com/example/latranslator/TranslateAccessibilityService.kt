package com.example.latranslator

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.Toast
import com.example.latranslator.data.data_source.LanguagesHelper
import com.example.latranslator.data.repositories.LanguagesRepository
import com.example.latranslator.data.repositories.ParametersRepository
import com.example.latranslator.databinding.TranslationLayoutBinding
import com.example.latranslator.utils.createLayoutParameters
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class TranslateAccessibilityService : AccessibilityService() {

    private var frameActionMovedFlag = false

    private var xOverlayOffset = 0
    private var yOverlayOffset = 0

    private lateinit var windowManager: WindowManager
    private lateinit var translationOverlayLayoutBinding: TranslationLayoutBinding
    private lateinit var overlayFrameLayout: FrameLayout

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        translationOverlayLayoutBinding = TranslationLayoutBinding.inflate(LayoutInflater.from(this))
        overlayFrameLayout = translationOverlayLayoutBinding.root

        overlayFrameLayout.setOnClickListener {
            if(!frameActionMovedFlag) {
                windowManager.removeViewImmediate(overlayFrameLayout)
            }
        }

        overlayFrameLayout.setOnLongClickListener {
            if(!frameActionMovedFlag) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val className = javaClass.name
                val clip = ClipData.newPlainText(className, translationOverlayLayoutBinding.textTranslation.text)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this@TranslateAccessibilityService, R.string.text_copied, Toast.LENGTH_LONG).show()
            }

            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if(overlayFrameLayout.isShown) {
            windowManager.removeViewImmediate(overlayFrameLayout)
        }

        runBlocking {

            xOverlayOffset = ParametersRepository
                .getTranslationFrameCurrentOffsetX(this@TranslateAccessibilityService) ?: 0

            yOverlayOffset = ParametersRepository
                .getTranslationFrameCurrentOffsetY(this@TranslateAccessibilityService) ?: 0

            val source = event.source

            val selectionStart = source?.textSelectionStart ?: return@runBlocking
            val selectionEnd = source.textSelectionEnd

            if(!source.isFocused)return@runBlocking

            val radius = ParametersRepository
                .getTranslationFrameCornerRadius(this@TranslateAccessibilityService) ?: 0f

            val shape = ShapeDrawable(
                RoundRectShape(
                    floatArrayOf(
                        radius,
                        radius,
                        radius,
                        radius,
                        radius,
                        radius,
                        radius,
                        radius
                    ), null, null
                )
            )

            translationOverlayLayoutBinding.textTranslation.textSize = ParametersRepository
                .getTranslationFrameTextSize(this@TranslateAccessibilityService) ?: 24.0f

            val backgroundColor = ParametersRepository.getFrameBackgroundColor(this@TranslateAccessibilityService) ?: -4203791
            translationOverlayLayoutBinding.root.backgroundTintList = ColorStateList.valueOf(backgroundColor)

            val textColor = ParametersRepository.getFrameTextColor(this@TranslateAccessibilityService) ?: -12961222
            translationOverlayLayoutBinding.textTranslation.setTextColor(textColor)
            overlayFrameLayout.background = shape

            val sourceLanguage = LanguagesRepository
                .getSourceLanguageKey(this@TranslateAccessibilityService) ?: return@runBlocking

            val targetLanguage = LanguagesRepository
                .getTargetLanguageKey(this@TranslateAccessibilityService) ?: return@runBlocking

            val translator = LanguagesHelper.getTranslatorClient(sourceLanguage, targetLanguage)

            if(selectionStart >= selectionEnd)return@runBlocking

            val selectedText = source.text.substring(selectionStart, selectionEnd)

            val params = createLayoutParameters(xOverlayOffset, yOverlayOffset)
            translator.translate(selectedText).addOnSuccessListener {
                translationOverlayLayoutBinding.textTranslation.text = it
                windowManager.addView(overlayFrameLayout, params)
            }

            overlayFrameLayout.setOnTouchListener(object : View.OnTouchListener {

                var x: Int = 0
                var y: Int = 0
                var touchedX: Float = 0.0F
                var touchedY: Float = 0.0F

                val updatedParams = params

                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    event?.let {
                        when(it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                x = updatedParams.x
                                y = updatedParams.y

                                touchedX = it.rawX
                                touchedY = it.rawY
                            }
                            MotionEvent.ACTION_MOVE -> {

                                val xNewPosition = (x + it.rawX - touchedX).toInt()
                                val yNewPosition = (y + it.rawY - touchedY).toInt()

                                if(xOverlayOffset + 3 == xNewPosition
                                    || yOverlayOffset + 3 == yNewPosition) {
                                    frameActionMovedFlag = true
                                }

                                xOverlayOffset = xNewPosition
                                yOverlayOffset = yNewPosition

                                updatedParams.x = xOverlayOffset
                                updatedParams.y = yOverlayOffset

                                windowManager.updateViewLayout(overlayFrameLayout, updatedParams)
                            }
                            MotionEvent.ACTION_UP -> {
                                frameActionMovedFlag = false
                                CoroutineScope(Dispatchers.IO).launch {
                                    ParametersRepository.setTranslationFrameCurrentOffsetX(this@TranslateAccessibilityService, xOverlayOffset)
                                    ParametersRepository.setTranslationFrameCurrentOffsetY(this@TranslateAccessibilityService, yOverlayOffset)
                                }
                            }
                            else -> {}
                        }
                    }

                    return false
                }
            })
        }
    }

    override fun onInterrupt() {
    }
}