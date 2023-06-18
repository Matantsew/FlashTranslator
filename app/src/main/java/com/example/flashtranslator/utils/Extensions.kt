package com.example.flashtranslator.utils

import android.view.View

fun View.visible(v: Boolean){
    visibility = if(v) View.VISIBLE else View.GONE
}