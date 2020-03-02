package com.hedvig.app.feature.marketpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.feature.language.LanguageViewModel
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.language_item_new.view.*

class LanguageAdapterNew(
    private val languageViewModel: LanguageViewModel,
    private val selectedCountry: Country
) : RecyclerView.Adapter<LanguageAdapterNew.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.language_item_new, parent, false)
    )

    override fun getItemCount() = 2

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            EN -> holder.apply {
                language.text =
                    language.resources.getString(R.string.SETTINGS_LANGUAGE_ENGLISH)
                parent.setHapticClickListener {
                    languageViewModel.selectLanguage(Language.EN_SE)
                }
            }
            LOCAL -> {
                when (selectedCountry) {
                    Country.SV -> {
                        holder.apply {
                            language.text =
                                language.resources.getString(R.string.SETTINGS_LANGUAGE_SWEDISH)
                            parent.setHapticClickListener {
                                languageViewModel.selectLanguage(Language.SV_SE)
                            }
                        }
                    }
                    Country.NO -> {
                        holder.apply {
                            //TODO textkey
                            language.text = "Norska"
                            parent.setHapticClickListener {
                                //TODO fix correct language
                                languageViewModel.selectLanguage(Language.EN_SE)

                            }
                        }
                    }
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parent = view
        val language: TextView = view.language
    }

    companion object {
        private const val EN = 0
        private const val LOCAL = 1
    }
}
