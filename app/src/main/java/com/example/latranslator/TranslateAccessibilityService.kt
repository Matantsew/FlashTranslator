package com.example.latranslator

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.Gravity
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

    private lateinit var windowManager: WindowManager
    private lateinit var translationLayoutBinding: TranslationLayoutBinding
    private lateinit var frameLayout: FrameLayout

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        translationLayoutBinding = TranslationLayoutBinding.inflate(LayoutInflater.from(this))
        frameLayout = translationLayoutBinding.root
    }

    private fun createParameters(x: Int, y: Int): WindowManager.LayoutParams {

        val params = WindowManager.LayoutParams(
            0,
            0,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)

        params.x = x
        params.y = y
        params.gravity = Gravity.CENTER or Gravity.CENTER

        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT

        return params
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if(frameLayout.isShown) {
            windowManager.removeViewImmediate(frameLayout)
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

            translationLayoutBinding.textTranslation.textSize = ParametersRepository
                .getTranslationFrameTextSize(this@TranslateAccessibilityService) ?: 0f

            val backgroundColor = ParametersRepository.getFrameBackgroundColor(this@TranslateAccessibilityService) ?: 0
            translationLayoutBinding.root.backgroundTintList = ColorStateList.valueOf(backgroundColor)

            val textColor = ParametersRepository.getFrameTextColor(this@TranslateAccessibilityService) ?: 0
            translationLayoutBinding.textTranslation.setTextColor(textColor)

            shape.paint.color = Color.BLUE
            shape.paint.strokeWidth = 15f
            frameLayout.background = shape

            val sourceLanguage = LanguagesRepository
                .getSourceLanguageKey(this@TranslateAccessibilityService) ?: return@runBlocking

            val targetLanguage = LanguagesRepository
                .getTargetLanguageKey(this@TranslateAccessibilityService) ?: return@runBlocking

            val translator = LanguagesHelper.getTranslatorClient(sourceLanguage, targetLanguage)

            if(selectionStart >= selectionEnd)return@runBlocking

            val selectedText = source.text.substring(selectionStart, selectionEnd)
            val xOffset = event.scrollX
            val yOffset = event.scrollY

            val params = createParameters(xOffset, yOffset)
            translator.translate(selectedText).addOnSuccessListener {
                translationLayoutBinding.textTranslation.text = it
                windowManager.addView(frameLayout, params)
            }

            frameLayout.setOnTouchListener(object : View.OnTouchListener {

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

                                updatedParams.x = (x + it.rawX - touchedX).toInt()
                                updatedParams.y = (y + it.rawY - touchedY).toInt()

                                windowManager.updateViewLayout(frameLayout, updatedParams)
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