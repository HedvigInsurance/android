package com.hedvig.app.feature.marketpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.feature.language.LanguageAndMarketViewModel
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.language_item_new.view.*

class LanguageAdapterNew(
    private val languageAndMarketViewModel: LanguageAndMarketViewModel,
    private val selectedMarket: Market
) : RecyclerView.Adapter<LanguageAdapterNew.ViewHolder>() {
    private var lastChecked: RadioButton? = null
    private var lastCheckedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.language_item_new, parent, false)
    )

    override fun getItemCount() = Market.values().size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        languageAndMarketViewModel.selectedLanguage.postValue(null)
        when (position) {
            LOCAL -> {
                when (selectedMarket) {
                    Market.SE -> {
                        holder.apply {
                            language.text =
                                language.resources.getString(R.string.SETTINGS_LANGUAGE_SWEDISH)
                            parent.setHapticClickListener {
                                languageAndMarketViewModel.selectLanguage(Language.SV_SE)
                            }
                        }
                    }
                    Market.NO -> {
                        holder.apply {
                            language.text = language.resources.getString(R.string.norwegian)
                            parent.setHapticClickListener {
                                languageAndMarketViewModel.selectLanguage(Language.NB_NO)

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
                EN -> {
                    when (selectedMarket) {
                        Market.SE -> {
                            languageAndMarketViewModel.selectLanguage(Language.EN_SE)
                        }
                        Market.NO -> {
                            languageAndMarketViewModel.selectLanguage(Language.EN_NO)
                        }
                    }
                }
                LOCAL -> {
                    when (selectedMarket) {
                        Market.SE -> {
                            languageAndMarketViewModel.selectLanguage(Language.SV_SE)
                        }
                        Market.NO -> {
                            languageAndMarketViewModel.selectLanguage(Language.NB_NO)
                        }
                    }
                }
            }
            val rb = v.radioButton
            rb.isChecked = true
            rb.background = rb.context.getDrawable(R.drawable.ic_radio_button_checked)
            animateRadioButton(holder)
            if (rb.isChecked) {
                lastChecked?.let {
                    if (lastCheckedPos != position) {
                        lastChecked?.background =
                            rb.context.getDrawable(R.drawable.ic_radio_button_unchecked)
                        lastChecked?.isChecked = false
                        animateRadioButton(holder)
                    }
                }
                lastChecked = rb
                lastCheckedPos = position
            } else lastChecked = null
        }
    }

    private fun animateRadioButton(holder: ViewHolder) {
        holder.button.apply {
            scaleX = 0f
            scaleY = 0f
            spring(
                SpringAnimation.SCALE_X,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(1f)
            spring(
                SpringAnimation.SCALE_Y,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(1f)
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
