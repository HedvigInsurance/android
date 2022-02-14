package com.hedvig.app.feature.claims.ui.commonclaim

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.load
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityEmergencyBinding
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.makeACall
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.startChat
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.disable
import com.hedvig.app.util.extensions.view.enable
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmergencyActivity : BaseActivity(R.layout.activity_emergency) {
    private val claimsViewModel: ClaimsViewModel by viewModel()
    private val binding by viewBinding(ActivityEmergencyBinding::bind)
    private val imageLoader: ImageLoader by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getParcelableExtra<EmergencyData>(EMERGENCY_DATA)

        if (data == null) {
            e { "Programmer error: No EMERGENCY_DATA passed to ${this.javaClass}" }
            return
        }

        claimsViewModel.events
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    ClaimsViewModel.Event.Error -> showErrorDialog(getString(R.string.component_error)) {}
                    ClaimsViewModel.Event.StartChat -> startChat()
                }
            }
            .launchIn(lifecycleScope)

        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)
            toolbar.applyStatusBarInsets()
            scrollView.applyNavigationBarInsets()

            toolbar.title = data.title
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            scrollView.setupToolbarScrollListener(toolbar = toolbar)

            val url = Uri.parse(data.iconUrls.iconByTheme(firstMessage.commonClaimFirstMessageIcon.context))
            firstMessage.commonClaimFirstMessageIcon.load(url, imageLoader)

            firstMessage.commonClaimFirstMessage.text =
                getString(R.string.COMMON_CLAIM_EMERGENCY_LAYOUT_TITLE)
            firstMessage.commonClaimCreateClaimButton.remove()

            if (data.eligibleToClaim) {
                showInsuranceActive(data.emergencyNumber)
            } else {
                showInsuranceInactive()
            }

            thirdEmergencyButton.setHapticClickListener {
                lifecycleScope.launch {
                    claimsViewModel.triggerFreeTextChat()
                }
            }
        }
    }

    private fun showInsuranceActive(emergencyNumber: String) {
        binding.apply {
            secondEmergencyButton.enable()
            secondEmergencyButton.setHapticClickListener {
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
