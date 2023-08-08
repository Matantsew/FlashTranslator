package com.example.flashtranslator.utils

import android.content.Context
import android.provider.Settings

fun isAccessibilityTurnedOn(context: Context, accessibilityServiceClass: Class<*>): Boolean {

    val prefString = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )

    return prefString != null && prefString.contains(context.packageName + "/" + accessibilityServiceClass.name)
}