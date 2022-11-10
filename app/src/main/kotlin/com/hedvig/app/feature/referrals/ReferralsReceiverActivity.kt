package com.hedvig.app.feature.referrals

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hedvig.app.R
import com.hedvig.app.databinding.ReferralsReceiverActivityBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.offer.usecase.CampaignCode
import com.hedvig.app.feature.referrals.ui.redeemcode.RedeemCodeViewModel
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import slimber.log.e

class ReferralsReceiverActivity : AppCompatActivity(R.layout.referrals_receiver_activity) {
  private val binding by viewBinding(ReferralsReceiverActivityBinding::bind)
  private val referralViewModel: RedeemCodeViewModel by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding.apply {
      lifecycleScope.launchWhenStarted {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
          referralViewModel.viewState.collect {
            it.data?.let { startChat() }
          }
        }
      }

      referralReceiverContinueButton.setHapticClickListener {
        val referralCode = intent.getStringExtra(EXTRA_REFERRAL_CODE)
        if (referralCode == null) {
          e { "Programmer error: EXTRA_REFERRAL_CODE not passed to ${this.javaClass}" }
          return@setHapticClickListener
        }
        referralViewModel.redeemReferralCode(CampaignCode(referralCode))
      }
      referralReceiverContinueWithoutButton.setHapticClickListener {
        startChat()
      }
      val incentive = intent.getStringExtra(EXTRA_REFERRAL_INCENTIVE)?.toBigDecimal()?.toInt()
      if (incentive == null) {
        e { "Programmer error: EXTRA_REFERRAL_INCENTIVE not passed to ${this.javaClass}" }
        return
      }
      referralsReceiverTitle.text =
        getString(hedvig.resources.R.string.REFERRAL_STARTSCREEN_HEADLINE, incentive)
    }
  }

  private fun startChat() {
    val intent = Intent(this, ChatActivity::class.java)
    intent.putExtra("intent", "onboarding")
    intent.putExtra("show_restart", true)
    startActivity(intent)
  }

  companion object {
    const val EXTRA_REFERRAL_CODE = "extra_referral_code"
    const val EXTRA_REFERRAL_INCENTIVE = "extra_referral_incentive"

    fun newInstance(context: Context, code: String, incentive: String) =
      Intent(context, ReferralsReceiverActivity::class.java).apply {
        putExtra(EXTRA_REFERRAL_CODE, code)
        putExtra(EXTRA_REFERRAL_INCENTIVE, incentive)
      }
  }
}
