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
        binding.recycler.adapter = HomeAdapter()

        model.data.observe(viewLifecycleOwner) { data ->
            // TODO: Show a proper error state if no first name is present.
            val firstName = data.member.firstName ?: throw Error("No first name")
            if (isPending(data.contracts)) {
                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.BigText.Pending(
                        firstName
                    )
                )
            }
            if (isActiveInFuture(data.contracts)) {
                @Suppress("SimplifiableCallChain") val firstInceptionDate = data
                    .contracts
                    .sortedBy { it.status.asActiveInFutureStatus?.futureInception }
                    .firstOrNull()
                    ?.status
                    ?.asActiveInFutureStatus
                    ?.futureInception
                    ?: throw Error("No future inception") // TODO: Show proper error state

                (binding.recycler.adapter as? HomeAdapter)?.items = listOf(
                    HomeModel.BigText.ActiveInFuture(
                        firstName,
                        firstInceptionDate
                    )
                )
            }
        }
    }

    companion object {
        fun isPending(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asPendingStatus != null }

        fun isActiveInFuture(contracts: List<HomeQuery.Contract>) =
            contracts.all { it.status.asActiveInFutureStatus != null }
    }
}

