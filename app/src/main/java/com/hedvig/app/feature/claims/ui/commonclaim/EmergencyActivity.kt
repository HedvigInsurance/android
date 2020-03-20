package com.hedvig.app.feature.claims.ui.commonclaim

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.makeACall
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.disable
import com.hedvig.app.util.extensions.view.enable
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.svg.buildRequestBuilder
import e
import kotlinx.android.synthetic.main.activity_emergency.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.common_claim_first_message.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class EmergencyActivity : BaseActivity() {
    private val claimsViewModel: ClaimsViewModel by viewModel()
    private val tracker: ClaimsTracker by inject()

    private val requestBuilder by lazy { buildRequestBuilder() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        val data = intent.getParcelableExtra<EmergencyData>(EMERGENCY_DATA)

        if (data == null) {
            e { "Programmer error: No EMERGENCY_DATA passed to ${this.javaClass}" }
            return
        }

        // TODO: This surface should be themed entirely I think
        val backgroundColor = colorAttr(R.attr.colorOnPrimary)
        setupLargeTitle(data.title, R.drawable.ic_back, backgroundColor) {
            onBackPressed()
        }
        appBarLayout.setExpanded(false, false)

        requestBuilder
            .load(
                Uri.parse(
                    BuildConfig.BASE_URL + data.iconUrls.iconByTheme(
                        commonClaimFirstMessageIcon.context
                    )
                )
            )
            .into(commonClaimFirstMessageIcon)

        commonClaimFirstMessageContainer.setBackgroundColor(backgroundColor)
        commonClaimFirstMessage.text = getString(R.string.CLAIMS_EMERGENCY_FIRST_MESSAGE)
        commonClaimCreateClaimButton.remove()

        when (data.insuranceStatus) {
            InsuranceStatus.ACTIVE -> showInsuranceActive()
            else -> showInsuranceInactive()
        }

        thirdEmergencyButton.setHapticClickListener {
            tracker.emergencyChat()
            claimsViewModel.triggerFreeTextChat {
                startClosableChat()
            }
        }
    }

    private fun showInsuranceActive() {
        secondEmergencyButton.enable()

        secondEmergencyButton.setHapticClickListener {
            tracker.callGlobalAssistance()
            makeACall(GLOBAL_ASSISTANCE_URI)
        }
    }

    private fun showInsuranceInactive() {
        secondEmergencyButton.disable()
    }

    companion object {
        private val GLOBAL_ASSISTANCE_URI = Uri.parse("tel:+4538489461")
        private const val EMERGENCY_DATA = "emergency_data"

        fun newInstance(context: Context, data: EmergencyData) =
            Intent(context, EmergencyActivity::class.java).apply {
                putExtra(EMERGENCY_DATA, data)
            }
    }
}


