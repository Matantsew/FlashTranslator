package com.example.latranslator

import android.annotation.SuppressLint
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import com.example.latranslator.data.data_source.LanguagesHelper
import com.example.latranslator.data.repositories.LanguagesRepository
import com.example.latranslator.data.repositories.ParametersRepository
import com.example.latranslator.databinding.TranslationLayoutBinding
import kotlinx.coroutines.runBlocking

class InstantTranslationService : Service() {

    private var frameActionMovedFlag = false

    private var xOverlayOffset = 0
    private var yOverlayOffset = 0

    private lateinit var windowManager: WindowManager
    private lateinit var translationOverlayLayoutBinding: TranslationLayoutBinding
    private lateinit var overlayFrameLayout: FrameLayout

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(overlayFrameLayout.isShown) {
            windowManager.removeViewImmediate(overlayFrameLayout)
        }

        runBlocking {
            val selectedText = intent?.extras?.getString(INTENT_EXTRA_PROCESS_TEXT) ?: ""
            onShowTranslationFrame(selectedText)
        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        translationOverlayLayoutBinding = TranslationLayoutBinding.inflate(LayoutInflater.from(this))
        overlayFrameLayout = translationOverlayLayoutBinding.root

        overlayFrameLayout.setOnClickListener {
            if(!frameActionMovedFlag) {
                windowManager.removeViewImmediate(overlayFrameLayout)
                stopSelf()
            }
        }

        overlayFrameLayout.setOnLongClickListener {
            if(!frameActionMovedFlag) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val className = javaClass.name
                val clip = ClipData.newPlainText(className, translationOverlayLayoutBinding.textTranslation.text)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this@InstantTranslationService, R.string.text_copied, Toast.LENGTH_LONG).show()
            }

            true
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    @SuppressLint("ClickableViewAccessibility")
    private suspend fun onShowTranslationFrame(selectedText: String) {

        val radius = ParametersRepository
            .getTranslationFrameCornerRadius(this@InstantTranslationService) ?: 0f

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
            .getTranslationFrameTextSize(this@InstantTranslationService) ?: 0f

        val backgroundColor = ParametersRepository.getFrameBackgroundColor(this@InstantTranslationService) ?: 0
        translationOverlayLayoutBinding.root.backgroundTintList = ColorStateList.valueOf(backgroundColor)

        val textColor = ParametersRepository.getFrameTextColor(this@InstantTranslationService) ?: 0
        translationOverlayLayoutBinding.textTranslation.setTextColor(textColor)
        overlayFrameLayout.background = shape

        val sourceLanguage = LanguagesRepository
            .getSourceLanguageKey(this@InstantTranslationService) ?: return

        val targetLanguage = LanguagesRepository
            .getTargetLanguageKey(this@InstantTranslationService) ?: return

        val translator = LanguagesHelper.getTranslatorClient(sourceLanguage, targetLanguage)

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
                        }
                    }
                }

                return false
            }
        })
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

    override fun onDestroy() {
        super.onDestroy()


    }
}