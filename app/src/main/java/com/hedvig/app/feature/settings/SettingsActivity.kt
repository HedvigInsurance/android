package com.hedvig.app.feature.settings

import android.annotation.SuppressLint
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
import androidx.preference.PreferenceManager
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.viewmodel.UserViewModel
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.service.LoginStatusService
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.triggerRestartActivity
import kotlinx.android.synthetic.main.activity_settings.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

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
        private val userViewModel: UserViewModel by sharedViewModel()
        @SuppressLint("ApplySharedPref")
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

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

            val marketPreference = findPreference<ListPreference>(SETTINGS_MARKET)
            marketPreference?.let { mp ->
                val oldValue = mp.value
                mp.setOnPreferenceChangeListener { preference, newValue ->
                    if (oldValue != newValue) {
                        //TODO real text for dialog
                        requireContext().showAlert(
                            R.string.alert_title,
                            R.string.alert_message,
                            positiveAction = {
    
                                sharedPreferences.edit()
                                    .putInt(
                                        Market.MARKET_SHARED_PREF,
                                        Market.valueOf(newValue.toString()).ordinal
                                    )
                                    .commit()

                                userViewModel.logout {
                                    requireContext().storeBoolean(
                                        LoginStatusService.IS_VIEWING_OFFER,
                                        false
                                    )
                                    requireContext().setAuthenticationToken(null)
                                    requireContext().setIsLoggedIn(false)
                                    FirebaseInstanceId.getInstance().deleteInstanceId()
                                    requireActivity().triggerRestartActivity()
                                }
                            },
                            negativeAction = {
                                mp.value = oldValue
                            }
                        )
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
        const val SETTINGS_MARKET = "market"
        fun newInstance(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}

