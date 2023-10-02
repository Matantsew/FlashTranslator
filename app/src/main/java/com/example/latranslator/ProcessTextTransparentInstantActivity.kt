package com.example.latranslator

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class ProcessTextTransparentInstantActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = intent
            .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)

        if(!text.isNullOrEmpty()) {
            val instantTranslationServiceIntent = Intent(this, InstantTranslationService::class.java)
            instantTranslationServiceIntent.putExtra(INTENT_EXTRA_PROCESS_TEXT, text.toString())
            startService(instantTranslationServiceIntent)
        }

        finishAndRemoveTask()
    }
}