package com.hedvig.app.feature.referrals.ui.tab

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.referrals.ReferralsViewModel
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.apollo.toWebLocaleTag
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showShareSheet
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import e
import kotlinx.android.synthetic.main.fragment_referrals.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class ReferralsFragment : Fragment(R.layout.fragment_referrals) {
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val referralsViewModel: ReferralsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shareInitialBottomMargin = share.marginBottom
        val invitesInitialBottomPadding = invites.paddingBottom

        loggedInViewModel.bottomTabInset.observe(this) { bti ->
            bti?.let { bottomTabInset ->
                share.updateMargin(bottom = shareInitialBottomMargin + bottomTabInset)
                invites.updatePadding(bottom = invitesInitialBottomPadding + bottomTabInset)
            }
        }

        invites.setupToolbarScrollListener(loggedInViewModel)
        invites.adapter = ReferralsAdapter {
            (invites.adapter as? ReferralsAdapter)?.setLoading()
            referralsViewModel.load()
        }

        referralsViewModel.data.observe(this) { data ->
            if (data == null) {
                return@observe
            }

            if (data.isFailure) {
                (invites.adapter as? ReferralsAdapter)?.items = listOf(
                    ReferralsModel.Title,
                    ReferralsModel.Error
                )
            }

            val successData = data.getOrNull() ?: return@observe

            val incentive =
                successData.referralInformation.campaign.incentive?.asMonthlyCostDeduction?.amount?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
            if (incentive == null) {
                e { "Invariant detected: referralInformation.campaign.incentive is null" }
            } else {
                val code = successData.referralInformation.campaign.code
                share.setHapticClickListener {
                    requireContext().showShareSheet(R.string.REFERRALS_SHARE_SHEET_TITLE) { intent ->
                        intent.putExtra(
                            Intent.EXTRA_TEXT,
                            requireContext().getString(
                                R.string.REFERRAL_SMS_MESSAGE,
                                incentive.format(requireContext()),
                                "${BuildConfig.WEB_BASE_URL}${defaultLocale(requireContext()).toWebLocaleTag()}/forever/${code}"
                            )
                        )
                        intent.type = "text/plain"
                    }
                }
                share.show()
                share
                    .animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }

            if (successData.referralInformation.invitations.isEmpty() && successData.referralInformation.referredBy == null) {
                (invites.adapter as? ReferralsAdapter)?.items = listOf(
                    ReferralsModel.Title,
                    ReferralsModel.Header.LoadedEmptyHeader(successData),
                    ReferralsModel.Code.LoadedCode(successData.referralInformation.campaign.code)
                )
                return@observe
            }

            val items = mutableListOf(
                ReferralsModel.Title,
                ReferralsModel.Header.LoadedHeader(successData),
                ReferralsModel.Code.LoadedCode(successData.referralInformation.campaign.code),
                ReferralsModel.InvitesHeader
            )

            items += successData.referralInformation.invitations.map {
                ReferralsModel.Referral.LoadedReferral(
                    it.fragments.referralFragment
                )
            }

            successData.referralInformation.referredBy?.let {
                items.add(ReferralsModel.Referral.Referee(it.fragments.referralFragment))
            }

            (invites.adapter as? ReferralsAdapter)?.items = items

        }
    }
}
