package com.hedvig.app.feature.referrals

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.BaseActivity
import com.hedvig.app.LoggedInActivity
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.hideStatusBar
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.referrals_successful_invite_actvity.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ReferralsSuccessfulInviteActivity : BaseActivity() {

    private val profileViewModel: ProfileViewModel by viewModel()
    private val tracker: ReferralsTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.referrals_successful_invite_actvity)

        //to get fresh data coming back to the app
        profileViewModel.refreshProfile()

        hideStatusBar()
        showSuccess()
        setupButtons()
    }

    private fun showSuccess() {
        referralSuccessImage.show()
        referralSuccessTitle.text = interpolateTextKey(
            getString(R.string.REFERRAL_SUCCESS_HEADLINE),
            "USER" to intent.getStringExtra(EXTRA_REFERRAL_NAME)
        )
        referralSuccessTitle.show()
        referralSuccessBody.text = interpolateTextKey(
            getString(R.string.REFERRAL_SUCCESS_BODY),
            "REFERRAL_VALUE" to intent.getStringExtra(EXTRA_REFERRAL_INCENTIVE).toBigDecimal().toInt()
        )
        referralSuccessBody.show()
    }

    private fun showUltimateSuccess() {
        referralSuccessRoot.setBackgroundColor(compatColor(R.color.yellow))
        referralUltimateSuccessImage.show()
        referralUltimateSuccessTitle.text = getString(R.string.REFERRAL_ULTIMATE_SUCCESS_TITLE)
        referralUltimateSuccessTitle.show()
        referralUltimateSuccessBody.text = getString(R.string.REFERRAL_ULTIMATE_SUCCESS_BODY)
        referralUltimateSuccessBody.show()
    }

    private fun setupButtons() {
        referralSuccessInvite.setHapticClickListener {
            tracker.inviteMoreFriends()
            val intent = Intent(this, LoggedInActivity::class.java)
            intent.putExtra(LoggedInActivity.EXTRA_IS_FROM_REFERRALS_NOTIFICATION, true)
            startActivity(intent)
        }
        referralSuccessCloseButton.setHapticClickListener {
            tracker.closeReferralSuccess()
            finish()
        }
    }

    companion object {
        const val EXTRA_REFERRAL_NAME = "extra_referral_name"
        const val EXTRA_REFERRAL_INCENTIVE = "extra_referral_incentive"

        fun newInstance(context: Context, name: String, incentive: String) = newInstance(context).apply {
            putExtra(EXTRA_REFERRAL_NAME, name)
            putExtra(EXTRA_REFERRAL_INCENTIVE, incentive)
        }

        fun newInstance(context: Context) = Intent(context, ReferralsSuccessfulInviteActivity::class.java)
    }
}
