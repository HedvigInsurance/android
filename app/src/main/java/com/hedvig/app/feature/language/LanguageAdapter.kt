package com.hedvig.app.feature.language

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BaseActivity.Companion.LOCALE_BROADCAST
import com.hedvig.app.R
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeLocaleString
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.language_item.view.*

class LanguageAdapter(
    private val tracker: LanguageSelectionTracker,
    private val languageAndMarketViewModel: LanguageAndMarketViewModel
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.language_item, parent, false)
    )

    override fun getItemCount() = 3

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            SYSTEM_DEFAULT -> holder.apply {
                language.text =
                    language.resources.getString(R.string.SETTINGS_LANGUAGE_SYSTEM_DEFAULT)
                flag.setImageDrawable(flag.context.compatDrawable(R.drawable.ic_flag_global))
                parent.setHapticClickListener {
                    tracker.selectLanguage("system_default")
                    parent.context.setLanguage(Language.SYSTEM_DEFAULT, languageAndMarketViewModel)
                    parent.context.goToMarketingActivity()
                }
            }
            SV_SE -> holder.apply {
                language.text = language.resources.getString(R.string.SETTINGS_LANGUAGE_SWEDISH)
                flag.setImageDrawable(flag.context.compatDrawable(R.drawable.ic_flag_se))
                parent.setHapticClickListener {
                    tracker.selectLanguage("se")
                    parent.context.setLanguage(Language.SV_SE, languageAndMarketViewModel)
                    parent.context.goToMarketingActivity()
                }
            }
            EN_SE -> holder.apply {
                language.text = language.resources.getString(R.string.SETTINGS_LANGUAGE_ENGLISH)
                flag.setImageDrawable(flag.context.compatDrawable(R.drawable.ic_flag_en))
                parent.setHapticClickListener {
                    tracker.selectLanguage("en")
                    parent.context.setLanguage(Language.EN_SE, languageAndMarketViewModel)
                    parent.context.goToMarketingActivity()
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parent = view
        val language: TextView = view.language
        val flag: ImageView = view.flag
    }

    companion object {
        private const val SYSTEM_DEFAULT = 0
        private const val SV_SE = 1
        private const val EN_SE = 2

        @SuppressLint("ApplySharedPref") // We want to apply this right away. It's important
        private fun Context.setLanguage(
            language: Language,
            languageAndMarketViewModel: LanguageAndMarketViewModel
        ) {
            PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit()
                .putString(SettingsActivity.SETTING_LANGUAGE, language.toString())
                .commit()

            language.apply(this)?.let { newContext ->
                languageAndMarketViewModel.updateLanguage(makeLocaleString(newContext))
            }

            LocalBroadcastManager
                .getInstance(this)
                .sendBroadcast(Intent(LOCALE_BROADCAST))
        }

        private fun Context.goToMarketingActivity() {
            startActivity(Intent(this, MarketingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }
    }
}
