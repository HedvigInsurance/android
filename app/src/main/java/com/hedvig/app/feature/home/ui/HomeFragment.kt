package com.hedvig.app.feature.home.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.HomeFragmentBinding
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.home_fragment) {
    private val model: HomeViewModel by viewModel()
    private val binding by viewBinding(HomeFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recycler.adapter = HomeAdapter(parentFragmentManager, model::load)

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
        }
    }

    companion object {
        fun isPending(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asPendingStatus != null }

        fun isActiveInFuture(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asActiveInFutureStatus != null || it.status.asActiveInFutureAndTerminatedInFutureStatus != null }

        fun isTerminated(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asTerminatedStatus != null }
    }
}
