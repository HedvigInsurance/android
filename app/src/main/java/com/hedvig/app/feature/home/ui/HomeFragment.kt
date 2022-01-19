package com.hedvig.app.feature.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import arrow.core.NonEmptyList
import coil.ImageLoader
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.R
import com.hedvig.app.databinding.HomeFragmentBinding
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.home.service.HomeTracker
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.home_fragment) {
    private val model: HomeViewModel by viewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val binding by viewBinding(HomeFragmentBinding::bind)
    private var scroll = 0
    private val tracker: HomeTracker by inject()
    private val imageLoader: ImageLoader by inject()
    private val marketManager: MarketManager by inject()
    private val featureManager: FeatureManager by inject()

    private val registerForActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            model.reload()
        }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.onScroll(scroll)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        scroll = 0

        val adapter = HomeAdapter(
            fragmentManager = parentFragmentManager,
            retry = model::load,
            startIntentForResult = ::startEmbarkForResult,
            imageLoader = imageLoader,
            tracker = tracker,
            marketManager = marketManager,
            areClaimCardsClickable = featureManager.isFeatureEnabled(Feature.CLAIMS_STATUS_V2),
        )

        binding.swipeToRefresh.setOnRefreshListener {
            model.reload()
        }

        binding.recycler.apply {
            applyNavigationBarInsets()
            applyStatusBarInsets()

            itemAnimator = ViewHolderReusingDefaultItemAnimator()
            this.adapter = adapter
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
            this.adapter = adapter
        }
        model.data.observe(viewLifecycleOwner) { (homeData, payinStatusData, pendingAddress) ->
            if (homeData == null) {
                return@observe
            }
            binding.swipeToRefresh.isRefreshing = homeData is HomeViewModel.ViewState.Loading
            if (homeData is HomeViewModel.ViewState.Error) {
                adapter.submitList(listOf(HomeModel.Error))
                return@observe
            }

            val successData = (homeData as? HomeViewModel.ViewState.Success)?.homeData ?: return@observe
            val firstName = successData.member.firstName
            if (firstName == null) {
                adapter.submitList(listOf(HomeModel.Error))
                return@observe
            }
            if (isPending(successData.contracts)) {
                val items = buildList {
                    add(HomeModel.BigText.Pending(firstName))
                    add(HomeModel.BodyText.Pending)
                    addClaimStatusCardsIfApplicable(successData)
                }
                adapter.submitList(items)
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
                    adapter.submitList(listOf(HomeModel.Error))
                    return@observe
                }

                val items = buildList {
                    add(HomeModel.BigText.ActiveInFuture(firstName, firstInceptionDate))
                    add(HomeModel.BodyText.ActiveInFuture)
                    addClaimStatusCardsIfApplicable(successData)
                }
                adapter.submitList(items)
            }

            if (isTerminated(successData.contracts)) {
                val items = buildList {
                    add(HomeModel.BigText.Terminated(firstName))
                    add(HomeModel.BodyText.Terminated)
                    val didAddClaimStatusCards = addClaimStatusCardsIfApplicable(successData)
                    if (didAddClaimStatusCards) {
                        add(HomeModel.StartClaimOutlined.NewClaim)
                    } else {
                        add(HomeModel.StartClaimContained.FirstClaim)
                    }
                    add(HomeModel.HowClaimsWork(successData.howClaimsWork))
                    if (pendingAddress != null && pendingAddress.isNotBlank()) {
                        add(HomeModel.PendingAddressChange(pendingAddress))
                    }
                    if (featureManager.isFeatureEnabled(Feature.MOVING_FLOW)) {
                        add(HomeModel.Header(getString(R.string.home_tab_editing_section_title)))
                        add(HomeModel.ChangeAddress(pendingAddress))
                    }
                }
                adapter.submitList(items)
            }

            if (isActive(successData.contracts)) {
                val items = buildList {
                    addAll(listOfNotNull(*psaItems(successData.importantMessages).toTypedArray()))
                    add(HomeModel.BigText.Active(firstName))
                    val didAddClaimStatusCards = addClaimStatusCardsIfApplicable(successData)
                    if (didAddClaimStatusCards) {
                        add(HomeModel.StartClaimOutlined.NewClaim)
                    } else {
                        add(HomeModel.StartClaimContained.FirstClaim)
                    }
                    add(HomeModel.HowClaimsWork(successData.howClaimsWork))
                    if (pendingAddress != null && pendingAddress.isNotBlank()) {
                        add(HomeModel.PendingAddressChange(pendingAddress))
                    }
                    addAll(listOfNotNull(*upcomingRenewals(successData.contracts).toTypedArray()))
                    if (payinStatusData?.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
                        add(HomeModel.ConnectPayin)
                    }
                    add(HomeModel.Header(getString(R.string.home_tab_common_claims_title)))
                    addAll(
                        listOfNotNull(
                            *commonClaimsItems(
                                successData.commonClaims,
                                successData.isEligibleToCreateClaim
                            ).toTypedArray()
                        )
                    )
                    if (featureManager.isFeatureEnabled(Feature.MOVING_FLOW)) {
                        add(HomeModel.Header(getString(R.string.home_tab_editing_section_title)))
                        add(HomeModel.ChangeAddress(pendingAddress))
                    }
                }
                adapter.submitList(items)
            }
        }
    }

    private fun startEmbarkForResult(intent: Intent) {
        registerForActivityResult.launch(intent)
    }

    /**
     * returns whether adding the claimStatusCards was applied or not
     */
    private fun MutableList<HomeModel>.addClaimStatusCardsIfApplicable(successData: HomeQuery.Data): Boolean {
        if (featureManager.isFeatureEnabled(Feature.CLAIMS_STATUS).not()) return false
        return NonEmptyList.fromList(successData.claimStatusCards)
            .map { claimStatusCardsQuery ->
                claimStatusCardsQuery.map(ClaimStatusCardUiState::fromClaimStatusCardsQuery)
            }
            .fold(
                ifEmpty = { false },
                ifSome = { claimStatusCardDataList ->
                    add(HomeModel.ClaimStatus(claimStatusCardDataList))
                    true
                },
            )
    }

    private fun psaItems(
        importantMessages: List<HomeQuery.ImportantMessage?>,
    ) = importantMessages
        .filterNotNull()
        .map { HomeModel.PSA(it) }

    private fun upcomingRenewals(contracts: List<HomeQuery.Contract>): List<HomeModel.UpcomingRenewal> =
        contracts.mapNotNull { c ->
            c.upcomingRenewal?.let {
                HomeModel.UpcomingRenewal(c.displayName, it)
            }
        }

    private fun commonClaimsItems(
        commonClaims: List<HomeQuery.CommonClaim>,
        isEligibleToCreateClaim: Boolean,
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
            contracts.all {
                it.status.asActiveInFutureStatus != null ||
                    it.status.asActiveInFutureAndTerminatedInFutureStatus != null
            }

        private fun isActive(contracts: List<HomeQuery.Contract>) =
            contracts.any {
                it.status.asActiveStatus != null ||
                    it.status.asTerminatedTodayStatus != null ||
                    it.status.asTerminatedInFutureStatus != null
            }

        private fun isTerminated(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asTerminatedStatus != null }
    }
}
