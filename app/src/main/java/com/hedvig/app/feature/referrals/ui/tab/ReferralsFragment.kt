package com.hedvig.app.feature.referrals.ui.tab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentReferralsBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.feature.profile.ui.charity.ExplanationBottomSheet
import com.hedvig.app.feature.referrals.service.ReferralsTracker
import com.hedvig.app.feature.referrals.ui.tab.ReferralsAdapter.Companion.ERROR_STATE
import com.hedvig.app.feature.referrals.ui.tab.ReferralsAdapter.Companion.LOADING_STATE
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.apollo.toWebLocaleTag
import com.hedvig.app.util.extensions.showShareSheet
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReferralsFragment : Fragment(R.layout.fragment_referrals) {
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val referralsViewModel: ReferralsViewModel by viewModel()
    private val tracker: ReferralsTracker by inject()
    private val marketManager: MarketManager by inject()
    private val localeManager: LocaleManager by inject()

    private val binding by viewBinding(FragmentReferralsBinding::bind)

    private var shareHeight = 0
    private var bottomTabInset = 0
    private var shareInitialBottomMargin = 0
    private var invitesInitialBottomPadding = 0
    private var scroll = 0

    override fun onResume() {
        super.onResume()
        loggedInViewModel.onScroll(scroll)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            shareInitialBottomMargin = share.marginBottom
            invitesInitialBottomPadding = invites.paddingBottom

            share.doOnLayout {
                shareHeight = it.height
                applyInsets()
            }

            scroll = 0
            invites.addOnScrollListener(
                ScrollPositionListener(
                    { scrollPosition ->
                        scroll = scrollPosition
                        loggedInViewModel.onScroll(scrollPosition)
                    },
                    viewLifecycleOwner
                )
            )

            invites.itemAnimator = ViewHolderReusingDefaultItemAnimator()
            val adapter = ReferralsAdapter(
                referralsViewModel::load,
                ::showBottomSheet,
                tracker,
                marketManager
            )
            invites.adapter = adapter

            swipeToRefresh.setOnApplyWindowInsetsListener { _, insets ->
                swipeToRefresh.setProgressViewOffset(
                    false,
                    0,
                    insets.systemWindowInsetTop + BASE_MARGIN_DOUBLE
                )
                insets
            }

            swipeToRefresh.setOnRefreshListener {
                referralsViewModel.setRefreshing(true)
                referralsViewModel.load()
            }

            referralsViewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
                swipeToRefresh.isRefreshing = isRefreshing
            }

            referralsViewModel
                .data
                .flowWithLifecycle(viewLifecycle)
                .onEach { viewState ->
                    when (viewState) {
                        ReferralsViewModel.ViewState.Error -> {
                            adapter.submitList(ERROR_STATE)
                        }
                        ReferralsViewModel.ViewState.Loading -> {
                            adapter.submitList(LOADING_STATE)
                        }
                        is ReferralsViewModel.ViewState.Success -> {

                            val incentive =
                                viewState
                                    .data
                                    .referralInformation
                                    .campaign
                                    .incentive
                                    ?.asMonthlyCostDeduction
                                    ?.amount
                                    ?.fragments
                                    ?.monetaryAmountFragment
                                    ?.toMonetaryAmount()
                            if (incentive == null) {
                                e { "Invariant detected: referralInformation.campaign.incentive is null" }
                            } else {
                                val code = viewState.data.referralInformation.campaign.code
                                share.setHapticClickListener {
                                    tracker.share()
                                    requireContext().showShareSheet(R.string.REFERRALS_SHARE_SHEET_TITLE) { intent ->
                                        intent.putExtra(
                                            Intent.EXTRA_TEXT,
                                            requireContext().getString(
                                                R.string.REFERRAL_SMS_MESSAGE,
                                                incentive.format(requireContext(), marketManager.market),
                                                "${
                                                requireContext().getString(R.string.WEB_BASE_URL)
                                                }/${
                                                localeManager.defaultLocale().toWebLocaleTag()
                                                }/forever/${
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

                            if (
                                viewState.data.referralInformation.invitations.isEmpty() &&
                                viewState.data.referralInformation.referredBy == null
                            ) {
                                (invites.adapter as? ReferralsAdapter)?.submitList(
                                    listOfNotNull(
                                        ReferralsModel.Title,
                                        if (viewState.showCampaignBar) ReferralsModel.ReferralTopBar(
                                            getString(R.string.REFERRAL_CAMPAIGN_BANNER_TITLE)
                                        ) else null,
                                        ReferralsModel.Header.LoadedEmptyHeader(viewState.data),
                                        ReferralsModel.Code.LoadedCode(viewState.data)
                                    )
                                )
                                return@onEach
                            }

                            val items = listOfNotNull(
                                ReferralsModel.Title,
                                if (viewState.showCampaignBar) ReferralsModel.ReferralTopBar(
                                    getString(R.string.REFERRAL_CAMPAIGN_BANNER_TITLE)
                                ) else null,
                                ReferralsModel.Header.LoadedHeader(viewState.data),
                                ReferralsModel.Code.LoadedCode(viewState.data),
                                ReferralsModel.InvitesHeader
                            ).toMutableList()

                            items += viewState.data.referralInformation.invitations
                                .filter {
                                    it.fragments.referralFragment.asActiveReferral != null ||
                                        it.fragments.referralFragment.asInProgressReferral != null ||
                                        it.fragments.referralFragment.asTerminatedReferral != null
                                }
                                .map {
                                    ReferralsModel.Referral.LoadedReferral(
                                        it.fragments.referralFragment
                                    )
                                }

                            viewState.data.referralInformation.referredBy?.let {
                                items.add(ReferralsModel.Referral.Referee(it.fragments.referralFragment))
                            }

                            adapter.submitList(items)
                        }
                    }
                }
                .launchIn(viewLifecycleScope)
        }
    }

    private fun showBottomSheet() {
        ExplanationBottomSheet.newInstance(
            title = getString(R.string.REFERRAL_CAMPAIGN_DETAIL_TITLE),
            markDownText = getString(R.string.REFERRAL_CAMPAIGN_DETAIL_BODY)
        ).show(parentFragmentManager, ExplanationBottomSheet.TAG)
    }

    private fun applyInsets() = with(binding) {
        share.updateMargin(bottom = shareInitialBottomMargin + bottomTabInset)
        invites.updatePadding(bottom = invitesInitialBottomPadding + bottomTabInset + shareHeight)
    }
}
