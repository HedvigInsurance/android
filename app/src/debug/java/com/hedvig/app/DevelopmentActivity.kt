package com.hedvig.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.language.LanguageSelectionActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.offer.OfferActivity
import com.hedvig.app.feature.profile.ui.payment.connect.ConnectPaymentActivity
import com.hedvig.app.feature.ratings.RatingsDialog
import com.hedvig.app.feature.referrals.ReferralsReceiverActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.mocks.mockModule
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.viewgallery.ViewGalleryActivity
import kotlinx.android.synthetic.debug.activity_development.*
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DevelopmentActivity : AppCompatActivity(R.layout.activity_development) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeButtons()
        initializeCheckboxes()
        initializeSpinners()
    }

    private fun initializeSpinners() {
        mockPersona.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        val persona = getSharedPreferences(DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)
        mockPersona.setSelection(persona)
    }

    private fun initializeButtons() {
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

        openConnectPayment.setHapticClickListener {
            startActivity(ConnectPaymentActivity.newInstance(this))
        }

        openChatActivity.setHapticClickListener {
            startActivity(ChatActivity.newInstance(this))
        }

        findViewById<Button>(R.id.openLoggedInWithWelcome).setHapticClickListener {
            startActivity(Intent(this, LoggedInActivity::class.java).apply {
                putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
            })
        }

        findViewById<Button>(R.id.openLoggedInWithoutWelcome).setHapticClickListener {
            startActivity(Intent(this, LoggedInActivity::class.java))
        }
        findViewById<Button>(R.id.openReferralReceiver).setHapticClickListener {
            startActivity(ReferralsReceiverActivity.newInstance(this, "CODE12", "10.00"))
        }

        findViewById<Button>(R.id.openNativeOffer).setHapticClickListener {
            startActivity(Intent(this, OfferActivity::class.java))
        }

        findViewById<Button>(R.id.openPostSignDD).setHapticClickListener {
            startActivity(ConnectPaymentActivity.newInstance(this, true))
        }

        findViewById<Button>(R.id.openVectorDrawableGallery).setHapticClickListener {
            startActivity(Intent(this, VectorDrawableGalleryActivity::class.java))
        }

        openViewGallery.setHapticClickListener {
            startActivity(ViewGalleryActivity.newInstance(this))
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

        notifications.setHapticClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
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
            listOf(
                dashboardModule,
                marketingModule,
                offerModule,
                profileModule,
                paymentModule,
                keyGearModule,
                adyenModule
            )
    }
}
