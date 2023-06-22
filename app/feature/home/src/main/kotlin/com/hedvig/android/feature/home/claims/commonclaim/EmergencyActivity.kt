package com.hedvig.android.feature.home.claims.commonclaim

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.disable
import com.hedvig.android.core.common.android.enable
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.android.core.common.android.remove
import com.hedvig.android.core.common.android.setupToolbarScrollListener
import com.hedvig.android.feature.home.R
import com.hedvig.android.feature.home.databinding.ActivityEmergencyBinding
import com.hedvig.android.navigation.activity.ActivityNavigator
import dev.chrisbanes.insetter.applyInsetter
import org.koin.android.ext.android.inject

class EmergencyActivity : AppCompatActivity(R.layout.activity_emergency) {

  private val activityNavigator: ActivityNavigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    val binding = ActivityEmergencyBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))

    val data = intent.parcelableExtra<EmergencyData>(EMERGENCY_DATA)
      ?: error("Programmer error: No EMERGENCY_DATA passed to ${this.javaClass}")

    WindowCompat.setDecorFitsSystemWindows(window, false)
    binding.toolbar.applyInsetter { type(statusBars = true) { padding() } }
    binding.scrollView.applyInsetter { type(navigationBars = true) { padding() } }

    binding.toolbar.title = data.title
    binding.toolbar.setNavigationOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }
    binding.scrollView.setupToolbarScrollListener(toolbar = binding.toolbar)

    binding.firstMessage.commonClaimFirstMessage.text =
      getString(hedvig.resources.R.string.COMMON_CLAIM_EMERGENCY_LAYOUT_TITLE)
    binding.firstMessage.commonClaimCreateClaimButton.remove()

    if (data.eligibleToClaim) {
      showInsuranceActive(binding, data.emergencyNumber)
    } else {
      showInsuranceInactive(binding)
    }

    binding.thirdEmergencyButton.setOnClickListener {
      activityNavigator.navigateToChat(this@EmergencyActivity)
    }
  }

  private fun showInsuranceActive(binding: ActivityEmergencyBinding, emergencyNumber: String) {
    binding.apply {
      secondEmergencyButton.enable()
      secondEmergencyButton.setOnClickListener {
        makeACall(Uri.parse("tel:$emergencyNumber"))
      }
    }
  }

  private fun showInsuranceInactive(binding: ActivityEmergencyBinding) {
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

private fun Activity.makeACall(uri: Uri) {
  val intent = Intent(Intent.ACTION_DIAL)
  intent.data = uri
  startActivity(intent)
}
