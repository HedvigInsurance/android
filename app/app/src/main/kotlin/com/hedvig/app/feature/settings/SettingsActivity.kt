package com.hedvig.app.feature.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.preferences.PreferenceKey
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.R
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.databinding.ActivitySettingsBinding
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import slimber.log.d

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {
  private val binding by viewBinding(ActivitySettingsBinding::bind)

  @SuppressLint("ApplySharedPref")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    binding.toolbar.setNavigationOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }
    setTheme(R.style.Hedvig_Theme_Settings)
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.preferenceContainer, PreferenceFragment())
      .commit()
  }

  class PreferenceFragment : PreferenceFragmentCompat() {
    private val marketManager: MarketManager by inject()
    private val viewModel: SettingsViewModel by activityViewModel()
    private val languageService: LanguageService by inject()
    private val logoutUseCase: LogoutUseCase by inject()

    @SuppressLint("ApplySharedPref")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.preferences, rootKey)

      getViewModel<SettingsViewModel>()

      val market = marketManager.market ?: return logoutUseCase.invoke()

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
      marketPreference?.let { mp ->
        mp.icon = market.flag.let { requireContext().compatDrawable(it) }
        mp.summary = getString(market.label)
        mp.setOnPreferenceClickListener {
          requireContext().showAlert(
            hedvig.resources.R.string.SETTINGS_ALERT_CHANGE_MARKET_TITLE,
            hedvig.resources.R.string.SETTINGS_ALERT_CHANGE_MARKET_TEXT,
            positiveLabel = hedvig.resources.R.string.ALERT_OK,
            negativeLabel = hedvig.resources.R.string.SETTINGS_ALERT_CHANGE_MARKET_CANCEL,
            positiveAction = {
              d { "Settings activity, changing country and therefore logging out" }
              logoutUseCase.invoke()
            },
          )
          true
        }
      }

      val languagePreference = findPreference<ListPreference>(PreferenceKey.SETTING_LANGUAGE)
      languagePreference?.let { lp ->
        lp.value = languageService.getLanguage().toString()
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
          Market.FR -> {}
        }
        lp.setOnPreferenceChangeListener { _, newValue ->
          (newValue as? String)?.let { v ->
            val language = Language.from(v)
            viewModel.applyLanguage(language)
          }
          false
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
              },
            )
          } else {
            startActivity(
              Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.fromParts("package", requireContext().packageName, null)
              },
            )
          }
          true
        }
      }
    }
  }

  companion object {
    const val SYSTEM_DEFAULT = "system_default"
    private const val SETTING_THEME = "theme"
    private const val SETTING_NOTIFICATIONS = "notifications"
    private const val SETTINGS_MARKET = "market"
    fun newInstance(context: Context) = Intent(context, SettingsActivity::class.java)
  }
}
