package com.hedvig.app.feature.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferenceContainer, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val themePreference = findPreference<ListPreference>(SETTING_THEME)
            themePreference?.let { tp ->
                if (tp.value == null) {
                    tp.value = Theme.SYSTEM_DEFAULT.toString()
                }
                tp.setOnPreferenceChangeListener { _, newValue ->
                    (newValue as? String)?.let { v ->
                        Theme
                            .from(v)
                            .apply()

                    }
                    true
                }
            }

            val languagePreference = findPreference<ListPreference>(SETTING_LANGUAGE)
            languagePreference?.let { lp ->
                if (lp.value == null) {
                    lp.value = Language.SWEDISH.toString()
                }
                lp.setOnPreferenceChangeListener { _, newValue ->
                    (newValue as? String)?.let { v ->
                        Language
                            .from(v)
                            .apply(requireContext())
                    }
                    true
                }
            }
        }
    }

    companion object {
        const val SETTING_THEME = "theme"
        const val SETTING_LANGUAGE = "language"
        fun newInstance(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}

