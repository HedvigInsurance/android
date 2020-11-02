package com.hedvig.app.feature.insurance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.DashboardUpsellBinding
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.databinding.InsuranceErrorBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.getActivity
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class InsuranceAdapter(
    private val tracker: InsuranceTracker,
    private val retry: () -> Unit
) :
    ListAdapter<InsuranceModel, InsuranceAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.insurance_contract_card -> ViewHolder.ContractViewHolder(parent)
        R.layout.dashboard_upsell -> ViewHolder.UpsellViewHolder(parent)
        R.layout.insurance_header -> ViewHolder.TitleViewHolder(parent)
        R.layout.insurance_error -> ViewHolder.Error(parent)
        R.layout.insurance_terminated_contracts_header -> ViewHolder.TerminatedContractsHeader(
            parent
        )
        R.layout.insurance_terminated_contracts -> ViewHolder.TerminatedContracts(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), retry, tracker)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is InsuranceModel.Contract -> R.layout.insurance_contract_card
        is InsuranceModel.Upsell -> R.layout.dashboard_upsell
        is InsuranceModel.Header -> R.layout.insurance_header
        InsuranceModel.TerminatedContractsHeader -> R.layout.insurance_terminated_contracts_header
        InsuranceModel.TerminatedContracts -> R.layout.insurance_terminated_contracts
        InsuranceModel.Error -> R.layout.insurance_error
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: InsuranceModel, retry: () -> Unit, tracker: InsuranceTracker): Any?

        fun invalid(data: InsuranceModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class UpsellViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.dashboard_upsell, parent, false)
        ) {
            private val binding by viewBinding(DashboardUpsellBinding::bind)

            init {
                binding.apply {
                    cta.setHapticClickListener {
                        val intent = ChatActivity.newInstance(cta.context, true)
                        val options =
                            ActivityOptionsCompat.makeCustomAnimation(
                                cta.context,
                                R.anim.activity_slide_up_in,
                                R.anim.stay_in_place
                            )

                        ActivityCompat.startActivity(cta.context, intent, options.toBundle())
                    }
                }
            }

            override fun bind(data: InsuranceModel, retry: () -> Unit, tracker: InsuranceTracker) =
                with(binding) {
                    if (data !is InsuranceModel.Upsell) {
                        return invalid(data)
                    }
                    title.setText(data.title)
                    description.setText(data.description)
                    cta.setText(data.ctaText)
                }
        }

        class ContractViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.insurance_contract_card, parent, false)
        ) {
            private val binding by viewBinding(InsuranceContractCardBinding::bind)

            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                tracker: InsuranceTracker
            ) = with(binding) {
                if (data !is InsuranceModel.Contract) {
                    return invalid(data)
                }
                data.inner.bindTo(binding)
                card.setHapticClickListener {
                    card.transitionName = TRANSITION_NAME
                    card.context.getActivity()?.let { activity ->
                        card.context.startActivity(
                            ContractDetailActivity.newInstance(
                                card.context,
                                data.inner.id
                            ),
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                activity,
                                card,
                                TRANSITION_NAME
                            ).toBundle()
                        )
                    }
                }
            }
        }

        class TitleViewHolder(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.insurance_header)) {
            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                tracker: InsuranceTracker
            ) = Unit
        }

        class Error(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.insurance_error)) {
            private val binding by viewBinding(InsuranceErrorBinding::bind)
            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                tracker: InsuranceTracker
            ): Any? = with(binding) {
                this.retry.setHapticClickListener {
                    tracker.retry()
                    retry()
                }
            }
        }

        class TerminatedContractsHeader(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.insurance_terminated_contracts_header)) {
            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                tracker: InsuranceTracker
            ) = Unit
        }

        class TerminatedContracts(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.insurance_terminated_contracts)) {
            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                tracker: InsuranceTracker
            ) = Unit
        }
    }

    companion object {
        private const val TRANSITION_NAME = "contract_card"
    }
}

sealed class InsuranceModel {
    object Header : InsuranceModel()

    data class Contract(
        val inner: InsuranceQuery.Contract
    ) : InsuranceModel()

    data class Upsell(
        @get:StringRes val title: Int,
        @get:StringRes val description: Int,
        @get:StringRes val ctaText: Int
    ) : InsuranceModel()

    object Error : InsuranceModel()

    object TerminatedContractsHeader : InsuranceModel()
    object TerminatedContracts : InsuranceModel()
}
