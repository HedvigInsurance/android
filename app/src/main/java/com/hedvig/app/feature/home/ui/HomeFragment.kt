package com.hedvig.app.feature.home.ui

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.R
import com.hedvig.app.databinding.HomeFragmentBinding
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.home.service.HomeTracker
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.home_fragment) {
    private val model: HomeViewModel by viewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val binding by viewBinding(HomeFragmentBinding::bind)
    private var scroll = 0
    private val tracker: HomeTracker by inject()

    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()
    private val marketProvider: MarketProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.onScroll(scroll)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        scroll = 0

        binding.recycler.apply {
            val recyclerInitialPaddingBottom = paddingBottom
            val recyclerInitialPaddingTop = paddingTop

            var hasInsetForToolbar = false

            loggedInViewModel.toolbarInset.observe(viewLifecycleOwner) { toolbarInsets ->
                updatePadding(top = recyclerInitialPaddingTop + toolbarInsets)
                if (!hasInsetForToolbar) {
                    hasInsetForToolbar = true
                    scrollToPosition(0)
                }
            }

            loggedInViewModel.bottomTabInset.observe(viewLifecycleOwner) { bottomTabInset ->
                updatePadding(bottom = recyclerInitialPaddingBottom + bottomTabInset)
            }
            adapter = HomeAdapter(
                parentFragmentManager,
                model::load,
                requestBuilder,
                tracker,
                marketProvider
            )
            (layoutManager as? GridLayoutManager)?.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        (binding.recycler.adapter as? HomeAdapter)?.currentList?.getOrNull(position)
                            ?.let { item ->
                                return when (item) {
                                    is HomeModel.CommonClaim -> 1
                                    else -> 2
                                }
                            }
                        return 2
                    }
                }
            addItemDecoration(HomeItemDecoration(context))
            addOnScrollListener(
                ScrollPositionListener(
                    { scrollPosition ->
                        scroll = scrollPosition
                        loggedInViewModel.onScroll(scrollPosition)
                    },
                    viewLifecycleOwner
                )
            )
        }

        model.data.observe(viewLifecycleOwner) { (homeData, payinStatusData) ->
            if (homeData == null) {
                return@observe
            }
            if (homeData.isFailure) {
                (binding.recycler.adapter as? HomeAdapter)?.submitList(listOf(HomeModel.Error))
                return@observe
            }

            val successData = homeData.getOrNull() ?: return@observe
            val firstName = successData.member.firstName
            if (firstName == null) {
                (binding.recycler.adapter as? HomeAdapter)?.submitList(listOf(HomeModel.Error))
                return@observe
            }
            if (isPending(successData.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.submitList(
                    listOf(
                        HomeModel.BigText.Pending(
                            firstName
                        ), HomeModel.BodyText.Pending
                    )
                )
            }
            if (isActiveInFuture(successData.contracts)) {
                val firstInceptionDate = successData
                    .contracts
                    .mapNotNull {
                        it.status.asActiveInFutureStatus?.futureInception
                            ?: it.status.asActiveInFutureAndTerminatedInFutureStatus?.futureInception
                    }
                    .minOrNull()

                if (firstInceptionDate == null) {
                    (binding.recycler.adapter as? HomeAdapter)?.submitList(listOf(HomeModel.Error))
                    return@observe
                }

                (binding.recycler.adapter as? HomeAdapter)?.submitList(
                    listOf(
                        HomeModel.BigText.ActiveInFuture(
                            firstName,
                            firstInceptionDate
                        ), HomeModel.BodyText.ActiveInFuture
                    )
                )
            }

            if (isTerminated(successData.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.submitList(
                    listOf(
                        HomeModel.BigText.Terminated(firstName),
                        HomeModel.BodyText.Terminated,
                        HomeModel.StartClaimOutlined,
                        HomeModel.HowClaimsWork(successData.howClaimsWork)
                    )
                )
            }

            if (isActive(successData.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.submitList(
                    listOfNotNull(
                        *psaItems(successData.importantMessages).toTypedArray(),
                        HomeModel.BigText.Active(firstName),
                        HomeModel.StartClaimContained,
                        HomeModel.HowClaimsWork(successData.howClaimsWork),
                        *upcomingRenewals(successData.contracts).toTypedArray(),
                        if (payinStatusData?.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
                            HomeModel.ConnectPayin
                        } else {
                            null
                        },
                        HomeModel.CommonClaimTitle,
                        *commonClaimsItems(
                            successData.commonClaims,
                            successData.isEligibleToCreateClaim
                        ).toTypedArray()
                    )
                )
            }
        }
    }

    private fun psaItems(
        importantMessages: List<HomeQuery.ImportantMessage?>
    ) = importantMessages
        .filterNotNull()
        .map { HomeModel.PSA(it) }

    private fun upcomingRenewals(contracts: List<HomeQuery.Contract>) =
        contracts.mapNotNull { c ->
            c.upcomingRenewal?.let {
                HomeModel.UpcomingRenewal(it)
            }
        }

    private fun commonClaimsItems(
        commonClaims: List<HomeQuery.CommonClaim>,
        isEligibleToCreateClaim: Boolean
    ) =
        commonClaims.map { cc ->
            cc.layout.asEmergency?.let {
                EmergencyData.from(cc, isEligibleToCreateClaim)?.let { ed ->
                    return@map HomeModel.CommonClaim.Emergency(ed)
                }
            }
            cc.layout.asTitleAndBulletPoints?.let {
                CommonClaimsData.from(cc, isEligibleToCreateClaim)
                    ?.let { ccd ->
                        return@map HomeModel.CommonClaim.TitleAndBulletPoints(ccd)
                    }
            }
            null
        }

    companion object {
        private fun isPending(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asPendingStatus != null }

        private fun isActiveInFuture(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asActiveInFutureStatus != null || it.status.asActiveInFutureAndTerminatedInFutureStatus != null }

        private fun isActive(contracts: List<HomeQuery.Contract>) =
            contracts.any { it.status.asActiveStatus != null || it.status.asTerminatedTodayStatus != null }

        private fun isTerminated(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asTerminatedStatus != null }
    }
}
