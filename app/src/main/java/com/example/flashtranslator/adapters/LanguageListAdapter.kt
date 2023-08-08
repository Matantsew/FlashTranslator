package com.example.flashtranslator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.flashtranslator.Language
import com.example.flashtranslator.GeneralViewModel
import com.example.flashtranslator.R
import com.example.flashtranslator.utils.visible
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LanguagesListAdapter(private val viewModel: GeneralViewModel)
    : RecyclerView.Adapter<LanguagesListAdapter.ViewHolder>() {

    private var languages: List<Language>? = null

    fun setLanguages(newSet: List<Language>) {
        languages = newSet
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItemCount(): Int {
        return languages?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val language = languages?.get(position) ?: return
        holder.bind(language, position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var languageNum: TextView
        private var languageText: MaterialTextView
        private var downloadedIcon: ShapeableImageView
        private var downloadDeleteButton: ImageButton
        private var progressCircularDownloadingLanguage: ProgressBar

        init {
            with(itemView){
                languageNum = findViewById(R.id.language_num)
                languageText = findViewById(R.id.language_text)
                downloadedIcon = findViewById(R.id.downloaded_icon)
                downloadDeleteButton = findViewById(R.id.download_delete_button)
                progressCircularDownloadingLanguage = findViewById(R.id.progress_circular_downloading_language)
            }
        }

        fun bind(language: Language, position: Int) {

            val number = (position + 1).toString() + "."

            languageNum.text = number
            languageText.text = language.toString()

            if(language.isDownloaded == true) {
                downloadedIcon.visible(true)
                downloadDeleteButton.setImageResource(R.drawable.ic_baseline_delete_24)
            }
            else {
                downloadedIcon.visible(false)
                downloadDeleteButton.setImageResource(R.drawable.ic_baseline_download_24)
            }

            viewModel.viewModelScope.launch(Dispatchers.Main) {
                viewModel.downloadingLanguagesKeysSet.collect { set ->
                    set.forEach { key ->
                        if (key == language.key) {
                            downloadDeleteButton.visible(false)
                            progressCircularDownloadingLanguage.visible(true)

                            onObservableDownloadLanguage(language)
                        }
                    }
                }
            }

            downloadDeleteButton.setOnClickListener {
                if(language.isDownloaded == false) {

                    progressCircularDownloadingLanguage.visible(true)
                    downloadDeleteButton.visible(false)

                    onObservableDownloadLanguage(language)
                }
                else {

                    progressCircularDownloadingLanguage.visible(true)
                    downloadDeleteButton.visible(false)

                    onObservableDeleteLanguageModel(language)
                }
            }
        }

        private fun onObservableDownloadLanguage(language: Language) {
            viewModel.obtainOrWaitLanguageModelRemotely(language.key) { onComplete ->
                if(onComplete) {
                    progressCircularDownloadingLanguage.visible(false)
                    downloadedIcon.visible(true)
                    downloadDeleteButton.setImageResource(R.drawable.ic_baseline_delete_24)
                    downloadDeleteButton.visible(true)

                    viewModel.obtainDownloadedLanguages()
                    Toast.makeText(itemView.context, "$language language is successfully downloaded", Toast.LENGTH_LONG).show()
                }
                else {
                    progressCircularDownloadingLanguage.visible(false)
                    downloadDeleteButton.visible(true)

                    Toast.makeText(itemView.context, "Downloading failed", Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun onObservableDeleteLanguageModel(language: Language) {

            viewModel.deleteLanguageModel(language.key) { onComplete ->
                if(onComplete) {
                    progressCircularDownloadingLanguage.visible(false)
                    downloadedIcon.visible(false)
                    downloadDeleteButton.setImageResource(R.drawable.ic_baseline_download_24)
                    downloadDeleteButton.visible(true)

                    viewModel.obtainDownloadedLanguages()
                    Toast.makeText(itemView.context, "$language language is successfully deleted", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}