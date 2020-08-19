package com.hedvig.app.feature.home.ui

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestBuilder
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.HomeFragmentBinding
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.home_fragment) {
    private val model: HomeViewModel by viewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val binding by viewBinding(HomeFragmentBinding::bind)

    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loggedInViewModel.bottomTabInset.observe(viewLifecycleOwner) { bottomTabInset ->
            binding.recycler.updatePadding(bottom = bottomTabInset)
        }
        binding.recycler.adapter = HomeAdapter(parentFragmentManager, model::load, requestBuilder)
        (binding.recycler.layoutManager as? GridLayoutManager)?.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    (binding.recycler.adapter as? HomeAdapter)?.items?.getOrNull(position)
                        ?.let { item ->
                            return when (item) {
                                is HomeModel.CommonClaim -> 1
                                else -> 2
                            }
                        }
                    return 2
                }
            }
        binding.recycler.addItemDecoration(HomeItemDecoration())

        model.data.observe(viewLifecycleOwner) { data ->
            if (data.isFailure) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.Error
                )
                return@observe
            }

            val successData = data.getOrNull() ?: return@observe
            val firstName = successData.member.firstName
            if (firstName == null) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.Error
                )
                return@observe
            }
            if (isPending(successData.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.BigText.Pending(
                        firstName
                    ),

                    HomeModel.BodyText.Pending
                )
            }
            if (isActiveInFuture(successData.contracts)) {
                val firstInceptionDate = successData
                    .contracts
                    .mapNotNull {
                        it.status.asActiveInFutureStatus?.futureInception
                            ?: it.status.asActiveInFutureAndTerminatedInFutureStatus?.futureInception
                    }
                    .min()

                if (firstInceptionDate == null) {
                    (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                        HomeModel.Error
                    )
                    return@observe
                }

                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.BigText.ActiveInFuture(
                        firstName,
                        firstInceptionDate
                    ),
                    HomeModel.BodyText.ActiveInFuture
                )
            }

            if (isTerminated(successData.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.BigText.Terminated(firstName),
                    HomeModel.StartClaimOutlined
                )
            }

            if (isActive(successData.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOfNotNull(
                    HomeModel.BigText.Active(firstName),
                    HomeModel.StartClaimContained,
                    HomeModel.CommonClaimTitle,
                    *(successData.commonClaims.map { cc ->
                        cc.layout.asEmergency?.let {
                            EmergencyData.from(cc, successData.isEligibleToCreateClaim)?.let { ed ->
                                return@map HomeModel.CommonClaim.Emergency(ed)
                            }
                        }
                        cc.layout.asTitleAndBulletPoints?.let {
                            CommonClaimsData.from(cc, successData.isEligibleToCreateClaim)
                                ?.let { ccd ->
                                    return@map HomeModel.CommonClaim.TitleAndBulletPoints(ccd)
                                }
                        }
                        null
                    }.toTypedArray())
                )
            }
        }
    }

    companion object {
        private fun isPending(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asPendingStatus != null }

        private fun isActiveInFuture(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asActiveInFutureStatus != null || it.status.asActiveInFutureAndTerminatedInFutureStatus != null }

        private fun isActive(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asActiveStatus != null }

        private fun isTerminated(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asTerminatedStatus != null }
    }
}
