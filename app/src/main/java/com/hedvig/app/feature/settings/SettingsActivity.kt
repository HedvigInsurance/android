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
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivitySettingsBinding
import com.hedvig.app.feature.chat.viewmodel.UserViewModel
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.makeLocaleString
import com.hedvig.app.service.LoginStatusService
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.triggerRestartActivity
import com.hedvig.app.util.extensions.viewBinding
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsActivity : BaseActivity(R.layout.activity_settings) {
    private val binding by viewBinding(ActivitySettingsBinding::bind)

    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        setTheme(R.style.Hedvig_Theme_Settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferenceContainer, PreferenceFragment())
            .commit()
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        private val mixpanel: MixpanelAPI by inject()
        private val marketManager: MarketManager by inject()
        private val userViewModel: UserViewModel by sharedViewModel()
        private val model: SettingsViewModel by viewModel()
        private val localeManager: LocaleManager by inject()

        @SuppressLint("ApplySharedPref")
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            val market = marketManager.market

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

            val marketPreference = findPreference<Preference>(SETTINGS_MARKET)
            if (market == null) {
                startActivity(MarketingActivity.newInstance(requireContext()))
            }
            marketPreference?.let { mp ->
                mp.icon = market?.flag?.let { requireContext().compatDrawable(it) }
                mp.summary = market?.label?.let { getString(it) }
                mp.setOnPreferenceClickListener {
                    requireContext().showAlert(
                        R.string.SETTINGS_ALERT_CHANGE_MARKET_TITLE,
                        R.string.SETTINGS_ALERT_CHANGE_MARKET_TEXT,
                        positiveLabel = R.string.SETTINGS_ALERT_CHANGE_MARKET_OK,
                        negativeLabel = R.string.SETTINGS_ALERT_CHANGE_MARKET_CANCEL,
                        positiveAction = {
                            marketManager.market = null
                            requireContext().storeBoolean(
                                MarketingActivity.HAS_SELECTED_MARKET,
                                false
                            )
                            userViewModel.logout {
                                requireContext().storeBoolean(
                                    LoginStatusService.IS_VIEWING_OFFER,
                                    false
                                )
                                requireContext().setAuthenticationToken(null)
                                requireContext().setIsLoggedIn(false)
                                runCatching { FirebaseInstanceId.getInstance().deleteInstanceId() }
                                mixpanel.reset()
                                requireActivity().triggerRestartActivity(MarketingActivity::class.java)
                            }
                        }
                    )
                    true
                }
            }

            val languagePreference = findPreference<ListPreference>(SETTING_LANGUAGE)
            languagePreference?.let { lp ->
                when (market) {
                    Market.SE -> {
                        lp.entries = resources.getStringArray(R.array.language_settings)
                        lp.entryValues = resources.getStringArray(R.array.language_settings_values)
                    }
                    Market.NO -> {
                        lp.entries = resources.getStringArray(R.array.language_settings_no)
                        lp.entryValues = resources.getStringArray(R.array.language_settings_values_no)
                    }
                    Market.DK -> {
                        lp.entries = resources.getStringArray(R.array.language_settings_dk)
                        lp.entryValues = resources.getStringArray(R.array.language_settings_values_dk)
                    }
                }
                lp.setOnPreferenceChangeListener { _, newValue ->
                    (newValue as? String)?.let { v ->
                        Language
                            .from(v)
                            .apply(requireContext()).let { ctx ->
                                model.save(makeLocaleString(ctx, marketManager.market), localeManager.defaultLocale())
                            }
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
                        startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                            }
                        )
                    } else {
                        startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                data = Uri.fromParts("package", requireContext().packageName, null)
                            }
                        )
                    }
                    true
                }
            }
        }
    }

    companion object {
        const val SYSTEM_DEFAULT = "system_default"
        const val SETTING_THEME = "theme"
        const val SETTING_LANGUAGE = "language"
        const val SETTING_NOTIFICATIONS = "notifications"
        const val SETTINGS_MARKET = "market"
        fun newInstance(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}
