package com.example.latranslator.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.latranslator.BuildConfig
import com.example.latranslator.GeneralViewModel
import com.example.latranslator.R
import com.example.latranslator.databinding.ActivityMainBinding
import com.example.latranslator.fragments.LanguagesFragment
import com.example.latranslator.fragments.SettingsFragment
import com.example.latranslator.fragments.TranslatorFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var turnOnItem: MenuItem
    private lateinit var aboutItem: MenuItem

    private val viewModel: GeneralViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.createConfigurationContext(this.resources.configuration)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        val adBannerView = AdView(this)

        adBannerView.setAdSize(AdSize.BANNER)
        adBannerView.adUnitId = if(BuildConfig.DEBUG) getString(R.string.ad_banner_debug) else getString(R.string.ad_banner_release)

        binding.adBannerFrame.addView(adBannerView)

        val adRequest = AdRequest.Builder().build()
        adBannerView.loadAd(adRequest)

        changeFragment<TranslatorFragment>(null)

        binding.bottomNavigation.setOnItemSelectedListener {

            val currentFragment = supportFragmentManager.findFragmentById(binding.container.id)?.apply {
                this::class.java
            } ?: Fragment()

            when(it.itemId) {
                R.id.translator_item -> {
                    changeFragment<TranslatorFragment>(currentFragment)
                    true
                }

                R.id.languages_item -> {
                    changeFragment<LanguagesFragment>(currentFragment)
                    true
                }

                R.id.settings_item -> {
                    changeFragment<SettingsFragment>(currentFragment)
                    true
                }

                else -> true
            }
        }

        checkDrawOverlayPermission()
    }

    private inline fun <reified F : Fragment> changeFragment(currentFragment: Fragment?) {
        supportFragmentManager.commit {

            currentFragment?.let { current ->
                if(current::class.java == F::class.java)return@commit

                when(current::class.java) {
                    TranslatorFragment::class.java -> {
                        setCustomAnimations(R.anim.from_right, R.anim.to_left)
                    }
                    LanguagesFragment::class.java -> {
                        when(F::class.java) {
                            TranslatorFragment::class.java -> setCustomAnimations(
                                R.anim.from_left,
                                R.anim.to_right
                            )
                            SettingsFragment::class.java -> setCustomAnimations(
                                R.anim.from_right,
                                R.anim.to_left
                            )
                            else -> {
                            }
                        }
                    }
                    SettingsFragment::class.java -> {
                        setCustomAnimations(R.anim.from_left, R.anim.to_right)
                    }
                    else -> return@commit
                }
            }

            setReorderingAllowed(true)
            replace<F>(binding.container.id)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission */
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            /** request permission via start activity for result */
            startActivityForResult(intent, -1010101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /** check if received result code
        is equal our requested code for draw permission  */
        if (requestCode == -1010101) {
            if (Settings.canDrawOverlays(this)) {
                // continue here - permission was granted
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.checkAccessibilityTurnedOn(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.open_accessibility_item -> {
                if(!viewModel.accessibilityTurnedOn.value) {
                    viewModel.showAccessibilityAlertDialog(this)
                }
                else viewModel.openAccessibilitySettings(this)
            }

            R.id.about_menu_item -> {

                //billingHelper?.launchBillingFlow(this)
                val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName

                val message = "${getText(R.string.version)} $version " +
                        "\n${getText(R.string.first_version_released)}"
                        "\n${getText(R.string.email)}"

                AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_logo)
                    .setTitle(R.string.app_name)
                    .setMessage(message)
                    .setNeutralButton(R.string.ok, null)
                    .show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu, menu)

        turnOnItem = menu.findItem(R.id.open_accessibility_item)
        aboutItem = menu.findItem(R.id.about_menu_item)

        return true
    }
}