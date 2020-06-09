package com.hedvig.app.feature.referrals

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import kotlinx.android.synthetic.main.fragment_new_referral.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ReferralsFragment : Fragment(R.layout.fragment_new_referral) {
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invites.setupToolbarScrollListener(loggedInViewModel)
        invites.adapter = ReferralsAdapter()
    }
}
