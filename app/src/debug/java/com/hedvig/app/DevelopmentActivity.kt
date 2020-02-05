package com.hedvig.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.keygear.ui.KeyGearValuationBottomSheet
import com.hedvig.app.feature.language.LanguageSelectionActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.offer.OfferActivity
import com.hedvig.app.feature.profile.ui.payment.TrustlyActivity
import com.hedvig.app.feature.ratings.RatingsDialog
import com.hedvig.app.feature.referrals.ReferralsReceiverActivity
import com.hedvig.app.feature.referrals.ReferralsSuccessfulInviteActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.debug.activity_development.*
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DevelopmentActivity : AppCompatActivity(R.layout.activity_development) {

    private val newsItem = WhatsNewQuery.News(
        "News",
        WhatsNewQuery.Illustration(
            "Icon", WhatsNewQuery.Variants(
                "IconVariants", WhatsNewQuery.Variants.Fragments(
                    IconVariantsFragment(
                        "IconVariants",
                        IconVariantsFragment.Dark(
                            "IconVariant",
                            "/app-content-service/whats_new_reward_dark.svg"
                        ),
                        IconVariantsFragment.Light(
                            "IconVariant",
                            "/app-content-service/whats_new_reward.svg"
                        )
                    )
                )
            )
        ),
        "Bonusregn till folket!",
        "Hedvig blir bättre när du får dela det med dina vänner! Du och dina vänner får lägre månadskostnad – för varje vän ni bjuder in!"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeButtons()
        initializeCheckboxes()
        initializeSpinners()
    }

    private fun initializeSpinners() {
        findViewById<Spinner>(R.id.mockPersona).onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    getSharedPreferences(DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
                        .edit()
                        .putInt("mockPersona", position)
                        .apply()
                }
            }
    }

    private fun initializeButtons() {
        findViewById<Button>(R.id.openWhatsNew).setHapticClickListener {
            WhatsNewDialog.newInstance(
                listOf(
                    newsItem, newsItem, newsItem
                )
            ).show(supportFragmentManager, "whats_new")
        }

        findViewById<Button>(R.id.openAlert).setHapticClickListener {
            showAlert(
                R.string.alert_title,
                R.string.alert_message,
                positiveAction = {
                    makeToast("Positive action activated")
                },
                negativeAction = {
                    makeToast("Negative action activated")
                }
            )
        }

        openValuation.setHapticClickListener {
            KeyGearValuationBottomSheet.newInstance()
                .show(supportFragmentManager, KeyGearValuationBottomSheet.TAG)
        }

        findViewById<Button>(R.id.openLoggedIn).setHapticClickListener {
            startActivity(Intent(this, LoggedInActivity::class.java).apply {
                putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
            })
        }
        findViewById<Button>(R.id.openReferralReceiver).setHapticClickListener {
            startActivity(ReferralsReceiverActivity.newInstance(this, "CODE12", "10.00"))
        }
        findViewById<Button>(R.id.openReferralNotification).setHapticClickListener {
            startActivity(ReferralsSuccessfulInviteActivity.newInstance(this, "Fredrik", "10.00"))
        }

        findViewById<Button>(R.id.openNativeChat).setHapticClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        findViewById<Button>(R.id.openNativeOffer).setHapticClickListener {
            startActivity(Intent(this, OfferActivity::class.java))
        }

        findViewById<Button>(R.id.openPostSignDD).setHapticClickListener {
            startActivity(TrustlyActivity.newInstance(this, true))
        }

        findViewById<Button>(R.id.openVectorDrawableGallery).setHapticClickListener {
            startActivity(Intent(this, VectorDrawableGalleryActivity::class.java))
        }

        findViewById<Button>(R.id.openSettings).setHapticClickListener {
            startActivity(SettingsActivity.newInstance(this))
        }

        findViewById<Button>(R.id.openRatingsDialog).setHapticClickListener {
            RatingsDialog
                .newInstance()
                .show(supportFragmentManager, RatingsDialog.TAG)
        }

        findViewById<Button>(R.id.openLanguageSelector).setHapticClickListener {
            startActivity(LanguageSelectionActivity.newInstance(this))
        }

        findViewById<TextInputEditText>(R.id.token).setText(getAuthenticationToken())

        findViewById<Button>(R.id.saveToken).setHapticClickListener {
            setAuthenticationToken(findViewById<TextInputEditText>(R.id.token).text.toString())
            makeToast("Token saved")
        }
    }

    private fun initializeCheckboxes() {
        val checkbox = findViewById<CheckBox>(R.id.useMockData)
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                unloadKoinModules(REAL_MODULES)
                loadKoinModules(mockModule)
                getSharedPreferences(DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("useMockData", true)
                    .apply()
            } else {
                unloadKoinModules(mockModule)
                loadKoinModules(REAL_MODULES)
                getSharedPreferences(DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("useMockData", false)
                    .apply()
            }
        }
        checkbox.isChecked = getSharedPreferences(DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean("useMockData", false)
    }

    companion object {
        const val DEVELOPMENT_PREFERENCES = "DevelopmentPreferences"
        private val REAL_MODULES =
            listOf(offerModule, profileModule, directDebitModule, keyGearModule)
    }
}
