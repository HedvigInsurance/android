package com.hedvig.app.feature.marketpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
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
    private var lastChecked: RadioButton? = null
    private var lastCheckedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.language_item_new, parent, false)
    )

    override fun getItemCount() = 2

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            LOCAL -> {
                when (selectedCountry) {
                    Country.SV -> {
                        holder.apply {
                            language.text =
                                language.resources.getString(R.string.SETTINGS_LANGUAGE_SWEDISH)
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
            EN -> holder.apply {
                language.text =
                    language.resources.getString(R.string.SETTINGS_LANGUAGE_ENGLISH)
            }
        }

        if (position == 0 && holder.button.isChecked) {
            lastChecked = holder.button
            lastCheckedPos = 0
        }

        holder.parent.setHapticClickListener { v ->
            when (position) {
                EN -> languageViewModel.selectLanguage(Language.EN_SE)
                LOCAL -> {
                    when (selectedCountry) {
                        Country.SV -> languageViewModel.selectLanguage(Language.SV_SE)
                        //TODO fix correct language
                        Country.NO -> languageViewModel.selectLanguage(Language.SV_SE)
                    }
                }
            }
            val rb = v.radioButton as RadioButton
            rb.isChecked = true
            if (rb.isChecked) {
                if (lastChecked != null) {
                    //TODO fixa !!
                    if (lastCheckedPos != position) {
                        lastChecked!!.isChecked = false
                    }
                }
                lastChecked = rb
                lastCheckedPos = position
            } else lastChecked = null
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parent = view
        val language: TextView = view.language
        val button: RadioButton = view.radioButton
    }

    companion object {
        private const val LOCAL = 0
        private const val EN = 1
    }
}
