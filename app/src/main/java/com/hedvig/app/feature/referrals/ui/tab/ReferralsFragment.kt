package com.hedvig.app.feature.referrals.ui.tab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.referrals.service.ReferralsTracker
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.apollo.toWebLocaleTag
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showShareSheet
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupToolbarAlphaScrollListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import e
import kotlinx.android.synthetic.main.fragment_referrals.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class ReferralsFragment : Fragment(R.layout.fragment_referrals) {
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val referralsViewModel: ReferralsViewModel by viewModel()
    private val tracker: ReferralsTracker by inject()

    private var shareHeight = 0
    private var bottomTabInset = 0
    private var shareInitialBottomMargin = 0
    private var invitesInitialBottomPadding = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shareInitialBottomMargin = share.marginBottom
        invitesInitialBottomPadding = invites.paddingBottom

        val scrollInitialTopPadding = invites.paddingTop
        loggedInViewModel.toolbarInset.observe(this) { tbi ->
            tbi?.let { toolbarInsets ->
                invites.updatePadding(top = scrollInitialTopPadding + toolbarInsets)
            }
        }

        share.doOnLayout {
            shareHeight = it.height
            applyInsets()
        }

        loggedInViewModel.bottomTabInset.observe(viewLifecycleOwner) { bti ->
            bti?.let {
                bottomTabInset = it
                applyInsets()
            }
        }

        invites.setupToolbarAlphaScrollListener(loggedInViewModel)
        invites.itemAnimator = ViewHolderReusingDefaultItemAnimator()
        invites.adapter = ReferralsAdapter({
            (invites.adapter as? ReferralsAdapter)?.setLoading()
            referralsViewModel.load()
        }, tracker)

        swipeToRefresh.setOnRefreshListener {
            referralsViewModel.setRefreshing(true)
            referralsViewModel.load()
        }

        referralsViewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            isRefreshing?.let { swipeToRefresh.isRefreshing = it }
        }

        referralsViewModel.data.observe(viewLifecycleOwner) { data ->
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
                    tracker.share()
                    requireContext().showShareSheet(R.string.REFERRALS_SHARE_SHEET_TITLE) { intent ->
                        intent.putExtra(
                            Intent.EXTRA_TEXT,
                            requireContext().getString(
                                R.string.REFERRAL_SMS_MESSAGE,
                                incentive.format(requireContext()),
                                "${BuildConfig.WEB_BASE_URL}${defaultLocale(requireContext()).toWebLocaleTag()}/forever/${
                                    Uri.encode(
                                        code
                                    )
                                }"
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
                    ReferralsModel.Code.LoadedCode(successData)
                )
                return@observe
            }

            val items = mutableListOf(
                ReferralsModel.Title,
                ReferralsModel.Header.LoadedHeader(successData),
                ReferralsModel.Code.LoadedCode(successData),
                ReferralsModel.InvitesHeader
            )

            items += successData.referralInformation.invitations
                .filter {
                    it.fragments.referralFragment.asActiveReferral != null
                        || it.fragments.referralFragment.asInProgressReferral != null
                        || it.fragments.referralFragment.asTerminatedReferral != null
                }
                .map {
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

    private fun applyInsets() {
        share.updateMargin(bottom = shareInitialBottomMargin + bottomTabInset)
        invites.updatePadding(bottom = invitesInitialBottomPadding + bottomTabInset + shareHeight)
    }

    companion object {
        private const val UTF_8 = "UTF-8"
    }
}
