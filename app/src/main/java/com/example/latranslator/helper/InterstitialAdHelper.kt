package com.example.latranslator.helper

import android.content.Context
import com.example.latranslator.BuildConfig
import com.example.latranslator.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdHelper {

    var mInterstitialAd: InterstitialAd? = null

    fun loadInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()

        val adId = if(BuildConfig.DEBUG) context.getString(R.string.ad_interstitial_debug) else context.getString(
            R.string.ad_interstitial_release)

        InterstitialAd.load(context, adId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }
}