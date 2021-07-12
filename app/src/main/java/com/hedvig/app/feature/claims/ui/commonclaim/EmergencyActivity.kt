package com.hedvig.app.feature.claims.ui.commonclaim

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityEmergencyBinding
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
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.svg.buildRequestBuilder
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmergencyActivity : BaseActivity(R.layout.activity_emergency) {
    private val claimsViewModel: ClaimsViewModel by viewModel()
    private val tracker: ClaimsTracker by inject()
    private val binding by viewBinding(ActivityEmergencyBinding::bind)

    private val requestBuilder by lazy { buildRequestBuilder() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getParcelableExtra<EmergencyData>(EMERGENCY_DATA)

        if (data == null) {
            e { "Programmer error: No EMERGENCY_DATA passed to ${this.javaClass}" }
            return
        }

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            scrollView.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }

            toolbar.title = data.title
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            scrollView.setupToolbarScrollListener(toolbar = toolbar)

            requestBuilder
                .load(
                    Uri.parse(
                        getString(R.string.BASE_URL) + data.iconUrls.iconByTheme(
                            firstMessage.commonClaimFirstMessageIcon.context
                        )
                    )
                )
                .into(firstMessage.commonClaimFirstMessageIcon)

            firstMessage.commonClaimFirstMessage.text =
                getString(R.string.COMMON_CLAIM_EMERGENCY_LAYOUT_TITLE)
            firstMessage.commonClaimCreateClaimButton.remove()

            if (data.eligibleToClaim) {
                showInsuranceActive(data.emergencyNumber)
            } else {
                showInsuranceInactive()
            }

            thirdEmergencyButton.setHapticClickListener {
                tracker.emergencyChat()
                lifecycleScope.launch {
                    claimsViewModel.triggerFreeTextChat()
                    startClosableChat()
                }
            }
        }
    }

    private fun showInsuranceActive(emergencyNumber: String) {
        binding.apply {
            secondEmergencyButton.enable()
            secondEmergencyButton.setHapticClickListener {
                tracker.callGlobalAssistance()
                makeACall(Uri.parse("tel:$emergencyNumber"))
            }
        }
    }

    private fun showInsuranceInactive() {
        binding.secondEmergencyButton.disable()
    }

    companion object {
        private const val EMERGENCY_DATA = "emergency_data"

        fun newInstance(context: Context, data: EmergencyData) =
            Intent(context, EmergencyActivity::class.java).apply {
                putExtra(EMERGENCY_DATA, data)
            }
    }
}
