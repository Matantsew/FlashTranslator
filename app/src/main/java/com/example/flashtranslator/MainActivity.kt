package com.example.flashtranslator

import android.app.AlertDialog
import android.content.Intent
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
import com.example.flashtranslator.databinding.ActivityMainBinding
import com.example.flashtranslator.fragments.LanguagesFragment
import com.example.flashtranslator.fragments.SettingsFragment
import com.example.flashtranslator.fragments.TranslatorFragment
import com.example.flashtranslator.utils.isAccessServiceEnabled
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var turnOnItem: MenuItem
    private lateinit var aboutItem: MenuItem

    private val viewModel: LanguagesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeFragment<TranslatorFragment>()

        binding.bottomNavigation.setOnItemSelectedListener {

            when(it.itemId){

                R.id.translator_item -> {
                    changeFragment<TranslatorFragment>()
                    true
                }

                R.id.languages_item -> {
                    changeFragment<LanguagesFragment>()
                    true
                }

                R.id.settings_item -> {
                    changeFragment<SettingsFragment>()
                    true
                }

                else -> true
            }
        }

        checkDrawOverlayPermission()
    }

    private inline fun <reified F : Fragment>changeFragment() {
        supportFragmentManager.commit {
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

    private fun showAccessibilityAlertDialog(){

        AlertDialog.Builder(this)
            .setTitle(R.string.accessibility_service_check_title)
            .setMessage(
                R.string.accessibility_service_opening
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Ok"
            ) { dialog, which -> openAccessibilitySettings() }
            .show()
    }

    private fun openAccessibilitySettings(){
        val accessibilitySettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(accessibilitySettingsIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.turn_on_translator_menu -> {

                if(!item.isChecked && !isAccessServiceEnabled(this, TranslateAccessibilityService::class.java)){

                    showAccessibilityAlertDialog()
                    return true
                }

                item.isChecked = !item.isChecked
            }

            R.id.about_menu -> {
                TODO("about menu")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu, menu)

        turnOnItem = menu.findItem(R.id.turn_on_translator_menu)
        aboutItem = menu.findItem(R.id.about_menu)

        return true
    }
}