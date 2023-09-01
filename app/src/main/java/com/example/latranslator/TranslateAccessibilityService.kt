package com.example.latranslator

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import com.example.latranslator.data.data_source.LanguagesHelper
import com.example.latranslator.data.repositories.LanguagesRepository
import com.example.latranslator.data.repositories.ParametersRepository
import com.example.latranslator.databinding.TranslationLayoutBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class TranslateAccessibilityService : AccessibilityService() {

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
    }

    private fun createParameters(x: Int, y: Int): WindowManager.LayoutParams {

        val params = WindowManager.LayoutParams(
            0,
            0,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)

        params.x = x
        params.y = y

        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT

        return params
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if(overlayFrameLayout.isShown) {
            windowManager.removeViewImmediate(overlayFrameLayout)
        }

        runBlocking {

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
                .getTranslationFrameTextSize(this@TranslateAccessibilityService) ?: 0f

            val backgroundColor = ParametersRepository.getFrameBackgroundColor(this@TranslateAccessibilityService) ?: 0
            translationOverlayLayoutBinding.root.backgroundTintList = ColorStateList.valueOf(backgroundColor)

            val textColor = ParametersRepository.getFrameTextColor(this@TranslateAccessibilityService) ?: 0
            translationOverlayLayoutBinding.textTranslation.setTextColor(textColor)
            overlayFrameLayout.background = shape

            val sourceLanguage = LanguagesRepository
                .getSourceLanguageKey(this@TranslateAccessibilityService) ?: return@runBlocking

            val targetLanguage = LanguagesRepository
                .getTargetLanguageKey(this@TranslateAccessibilityService) ?: return@runBlocking

            val translator = LanguagesHelper.getTranslatorClient(sourceLanguage, targetLanguage)

            if(selectionStart >= selectionEnd)return@runBlocking

            val selectedText = source.text.substring(selectionStart, selectionEnd)

            val params = createParameters(xOverlayOffset, yOverlayOffset)
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
                        when(it.action){
                            MotionEvent.ACTION_DOWN -> {
                                x = updatedParams.x
                                y = updatedParams.y

                                touchedX = it.rawX
                                touchedY = it.rawY
                            }
                            MotionEvent.ACTION_MOVE -> {
                                xOverlayOffset = (x + it.rawX - touchedX).toInt()
                                yOverlayOffset = (y + it.rawY - touchedY).toInt()

                                updatedParams.x = xOverlayOffset
                                updatedParams.y = yOverlayOffset

                                windowManager.updateViewLayout(overlayFrameLayout, updatedParams)
                            }
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