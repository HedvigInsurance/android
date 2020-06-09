package com.hedvig.app.feature.referrals

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.fragment_new_referral.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class ReferralsFragment : Fragment(R.layout.fragment_new_referral) {
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val referralsViewModel: ReferralsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invites.setupToolbarScrollListener(loggedInViewModel)
        invites.adapter = ReferralsAdapter()

        referralsViewModel.data.observe(this) { data ->
            if (data == null) {
                return@observe
            }

            // TODO: Animate the reveal
            share.show()

            if (data.referralInformation.invitations.isEmpty() && data.referralInformation.referredBy == null) {
                (invites.adapter as? ReferralsAdapter)?.items = listOf(
                    ReferralsModel.Header.LoadedEmptyHeader,
                    ReferralsModel.Code.LoadedCode(data.referralInformation.campaign.code)
                )
                return@observe
            }
        }
    }
}
