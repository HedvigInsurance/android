package com.hedvig.app.feature.claims.ui.commonclaim

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.util.extensions.makeACall
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.disable
import com.hedvig.app.util.extensions.view.enable
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.svg.buildRequestBuilder
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import kotlinx.android.synthetic.main.activity_emergency.*
import kotlinx.android.synthetic.main.common_claim_first_message.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class EmergencyActivity : BaseActivity(R.layout.activity_emergency) {
    private val claimsViewModel: ClaimsViewModel by viewModel()
    private val tracker: ClaimsTracker by inject()

    private val requestBuilder by lazy { buildRequestBuilder() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getParcelableExtra<EmergencyData>(EMERGENCY_DATA)

        if (data == null) {
            e { "Programmer error: No EMERGENCY_DATA passed to ${this.javaClass}" }
            return
        }

        root.setEdgeToEdgeSystemUiFlags(true)
        root.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
        }
        toolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }

        emergencyTitle.text = data.title
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        root.setupToolbarScrollListener(toolbar = toolbar)

        requestBuilder
            .load(
                Uri.parse(
                    BuildConfig.BASE_URL + data.iconUrls.iconByTheme(
                        commonClaimFirstMessageIcon.context
                    )
                )
            )
            .into(commonClaimFirstMessageIcon)

        commonClaimFirstMessage.text = getString(R.string.CLAIMS_EMERGENCY_FIRST_MESSAGE)
        commonClaimCreateClaimButton.remove()

        if (data.eligibleToClaim) {
            showInsuranceActive()
        } else {
            showInsuranceInactive()
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


