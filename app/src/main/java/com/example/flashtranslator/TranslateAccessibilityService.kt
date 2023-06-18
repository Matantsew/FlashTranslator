package com.example.flashtranslator

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.preferencesKey
import com.example.flashtranslator.databinding.TranslationLayoutBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class TranslateAccessibilityService : AccessibilityService() {

    private var isTranslatedTextViewShown = false

    private lateinit var wm: WindowManager
    private lateinit var viewBinding: TranslationLayoutBinding
    private lateinit var ll: FrameLayout

    override fun onCreate() {
        super.onCreate()

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        viewBinding = TranslationLayoutBinding.inflate(LayoutInflater.from(this))
        ll = viewBinding.root
    }

    private fun createParameters(x: Int, y: Int): WindowManager.LayoutParams{

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

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if(isTranslatedTextViewShown){
            wm.removeViewImmediate(ll)
            isTranslatedTextViewShown = false
        }

        runBlocking {

            val sourceTargetLanguagesDS = createDataStore(name = DATA_STORE_LANGUAGE_SOURCE_TARGET)

            val languagesStore = sourceTargetLanguagesDS.data.first()

            if(languagesStore.asMap().isEmpty())return@runBlocking

            val sourceLanguage = languagesStore.asMap().getValue(preferencesKey<Int>(
                SOURCE_LANGUAGE
            )) as String

            val targetLanguage = languagesStore.asMap().getValue(preferencesKey<Int>(
                TARGET_LANGUAGE
            )) as String

            //val translator = languagesRepository.getTranslator(sourceLanguage, targetLanguage)

            val source = event.source

            val selectionStart = source.textSelectionStart
            val selectionEnd = source.textSelectionEnd

            if(selectionStart >= selectionEnd)return@runBlocking

            val selectedText = source.text.substring(selectionStart, selectionEnd)
            val xOffset = event.scrollX
            val yOffset = event.scrollY

            val params = createParameters(xOffset, yOffset)
/*
            translator.translate(selectedText).addOnSuccessListener {

                viewBinding.textTranslation.text = it
                wm.addView(ll, params)
                shown = true
            }*/

            ll.setOnTouchListener(object : View.OnTouchListener {

                var x: Int = 0
                var y: Int = 0
                var touchedX: Float = 0.0F
                var touchedY: Float = 0.0F

                val updatedParams = params

                override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                    if (event != null) {
                        when(event.action){
                            MotionEvent.ACTION_DOWN -> {

                                x = updatedParams.x
                                y = updatedParams.y

                                touchedX = event.rawX
                                touchedY = event.rawY

                            }
                            MotionEvent.ACTION_MOVE -> {

                                updatedParams.x = (x + event.rawX - touchedX) as Int
                                updatedParams.y = (y + event.rawY - touchedY) as Int

                                wm.updateViewLayout(ll, updatedParams)
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