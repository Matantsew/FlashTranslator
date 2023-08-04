package com.example.flashtranslator

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.flashtranslator.data.data_source.LanguagesHelper
import com.example.flashtranslator.databinding.TranslationLayoutBinding
import com.example.flashtranslator.utils.obtainLanguageSourceTargetDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
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

        val LAYOUT_FLAG: Int = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams(
            0,
            0,
            LAYOUT_FLAG,
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

            val languagesStore = obtainLanguageSourceTargetDataStore.data.first().asMap()

            if(languagesStore.isEmpty())return@runBlocking

            val sourceLanguage = languagesStore[stringPreferencesKey(SOURCE_LANGUAGE_KEY_PREF_KEY)] as String
            val targetLanguage = languagesStore[stringPreferencesKey(TARGET_LANGUAGE_KEY_PREF_KEY)] as String

            val translator = LanguagesHelper.getTranslatorClient(sourceLanguage, targetLanguage)

            val source = event.source

            val selectionStart = source?.textSelectionStart ?: return@runBlocking
            val selectionEnd = source.textSelectionEnd

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