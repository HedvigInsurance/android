package com.hedvig.app.feature.referrals

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ChatActivity
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.referrals_receiver_activity.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ReferralsReceiverActivity : BaseActivity() {

    private val referralViewModel: ReferralViewModel by viewModel()
    private val tracker: ReferralsTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.referrals_receiver_activity)

        referralViewModel.apply {
            redeemCodeStatus.observe(lifecycleOwner = this@ReferralsReceiverActivity) { data ->
                if (data != null) {
                    startChat()
                } else {
                    // TODO let' create string for this
                    makeToast("The code ${intent.getStringExtra(EXTRA_REFERRAL_CODE)} is invalid!")
                }
            }
        }
        referralReceiverContinueButton.setHapticClickListener {
            tracker.redeemReferralCode()
            referralViewModel.redeemReferralCode(intent.getStringExtra(EXTRA_REFERRAL_CODE))
        }
        referralReceiverContinueWithoutButton.setHapticClickListener {
            tracker.skipReferralCode()
            startChat()
        }
        referralsReceiverTitle.text = interpolateTextKey(
            getString(R.string.REFERRAL_STARTSCREEN_HEADLINE),
            "REFERRAL_VALUE" to intent.getStringExtra(EXTRA_REFERRAL_INCENTIVE).toBigDecimal().toInt()
        )
        referralsReceiverBody.text = interpolateTextKey(
            getString(R.string.REFERRAL_STARTSCREEN_BODY),
            "REFERRAL_VALUE" to intent.getStringExtra(EXTRA_REFERRAL_INCENTIVE).toBigDecimal().toInt()
        )
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

        fun newInstance(context: Context, code: String, incentive: String) = Intent(context, ReferralsReceiverActivity::class.java).apply {
            putExtra(EXTRA_REFERRAL_CODE, code)
            putExtra(EXTRA_REFERRAL_INCENTIVE, incentive)
        }
    }
}
