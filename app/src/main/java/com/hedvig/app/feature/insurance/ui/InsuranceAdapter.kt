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

class InsuranceAdapter(
    private val tracker: InsuranceTracker,
    private val retry: () -> Unit
) : ListAdapter<InsuranceModel, InsuranceAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.insurance_contract_card -> ViewHolder.ContractViewHolder(parent)
        R.layout.dashboard_upsell -> ViewHolder.UpsellViewHolder(parent)
        R.layout.insurance_header -> ViewHolder.TitleViewHolder(parent)
        R.layout.insurance_error -> ViewHolder.Error(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ContractViewHolder -> {
                (getItem(position) as? InsuranceModel.Contract)?.let {
                    holder.bind(it.inner)
                }
            }
            is ViewHolder.UpsellViewHolder -> {
                (getItem(position) as? InsuranceModel.Upsell)?.let { holder.bind(it) }
            }
            is ViewHolder.Error -> {
                holder.bind(retry, tracker)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is InsuranceModel.Contract -> R.layout.insurance_contract_card
        is InsuranceModel.Upsell -> R.layout.dashboard_upsell
        is InsuranceModel.Header -> R.layout.insurance_header
        InsuranceModel.Error -> R.layout.insurance_error
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

            fun bind(model: InsuranceModel.Upsell) {
                binding.apply {
                    title.text = title.resources.getString(model.title)
                    description.text = description.resources.getString(model.description)
                    cta.text = cta.resources.getString(model.ctaText)
                }
            }
        }

        class ContractViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.insurance_contract_card, parent, false)
        ) {
            private val binding by viewBinding(InsuranceContractCardBinding::bind)

            fun bind(contract: InsuranceQuery.Contract) {
                contract.bindTo(binding)
                binding.card.apply {
                    setHapticClickListener {
                        transitionName = TRANSITION_NAME
                        context.getActivity()?.let { activity ->
                            context.startActivity(
                                ContractDetailActivity.newInstance(
                                    context,
                                    contract.id
                                ),
                                ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    activity,
                                    this,
                                    TRANSITION_NAME
                                ).toBundle()
                            )
                        }
                    }
                }
            }
        }

        class TitleViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.insurance_header, parent, false)
        )

        class Error(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.insurance_error)) {
            private val binding by viewBinding(InsuranceErrorBinding::bind)
            fun bind(retry: () -> Unit, tracker: InsuranceTracker): Any? = with(binding) {
                this.retry.setHapticClickListener {
                    tracker.retry()
                    retry()
                }
            }
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
}
