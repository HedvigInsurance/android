package com.hedvig.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.ListPreference
import androidx.preference.Preference
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
                    lp.value = Language.SYSTEM_DEFAULT.toString()
                }
                lp.setOnPreferenceChangeListener { _, newValue ->
                    (newValue as? String)?.let { v ->
                        Language
                            .from(v)
                            .apply(requireContext())
                        LocalBroadcastManager
                            .getInstance(requireContext())
                            .sendBroadcast(Intent(LOCALE_BROADCAST))
                    }
                    true
                }
            }

            val notificationsPreference = findPreference<Preference>(SETTING_NOTIFICATIONS)
            notificationsPreference?.let { np ->
                np.setOnPreferenceClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                        })
                    } else {
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            data = Uri.fromParts("package", requireContext().packageName, null)
                        })
                    }
                    true
                }
            }
        }
    }

    companion object {
        const val SETTING_THEME = "theme"
        const val SETTING_LANGUAGE = "language"
        const val SETTING_NOTIFICATIONS = "notifications"
        fun newInstance(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}

