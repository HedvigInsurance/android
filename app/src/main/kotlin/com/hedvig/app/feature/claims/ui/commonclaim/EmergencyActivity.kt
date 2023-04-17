package com.hedvig.app.feature.claims.ui.commonclaim

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.parcelableExtra
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmergencyActivity : AppCompatActivity(R.layout.activity_emergency) {
  private val claimsViewModel: ClaimsViewModel by viewModel()
  private val binding by viewBinding(ActivityEmergencyBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    val data = intent.parcelableExtra<EmergencyData>(EMERGENCY_DATA)
      ?: error("Programmer error: No EMERGENCY_DATA passed to ${this.javaClass}")

    claimsViewModel.events
      .flowWithLifecycle(lifecycle)
      .onEach { event ->
        when (event) {
          ClaimsViewModel.Event.Error -> {
            showErrorDialog(getString(com.adyen.checkout.dropin.R.string.component_error)) {}
          }
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
        onBackPressedDispatcher.onBackPressed()
      }
      scrollView.setupToolbarScrollListener(toolbar = toolbar)

      firstMessage.commonClaimFirstMessage.text =
        getString(hedvig.resources.R.string.COMMON_CLAIM_EMERGENCY_LAYOUT_TITLE)
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
