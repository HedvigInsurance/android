package com.hedvig.app.feature.insurance.ui

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.GenericErrorBinding
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.databinding.InsuranceTerminatedContractsBinding
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.detail.CrossSellDetailActivity
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.getActivity
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class InsuranceAdapter(
    private val marketManager: MarketManager,
    private val retry: () -> Unit,
    private val onClickCrossSell: (CrossSellData.Action) -> Unit,
) : ListAdapter<InsuranceModel, InsuranceAdapter.ViewHolder>(InsuranceAdapterDiffUtilItemCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.insurance_contract_card -> ViewHolder.ContractViewHolder(parent)
        CROSS_SELL -> ViewHolder.CrossSellViewHolder(ComposeView(parent.context), onClickCrossSell)
        R.layout.insurance_header -> ViewHolder.TitleViewHolder(parent)
        R.layout.generic_error -> ViewHolder.Error(parent)
        SUBHEADING -> ViewHolder.SubheadingViewHolder(ComposeView(parent.context))
        NOTIFICATION_SUBHEADING -> ViewHolder.NotificationSubheadingViewHolder(ComposeView(parent.context))
        R.layout.insurance_terminated_contracts -> ViewHolder.TerminatedContracts(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is InsuranceModel.Contract -> R.layout.insurance_contract_card
        is InsuranceModel.CrossSellCard -> CROSS_SELL
        is InsuranceModel.Header -> R.layout.insurance_header
        InsuranceModel.TerminatedContractsHeader -> SUBHEADING
        is InsuranceModel.CrossSellHeader -> NOTIFICATION_SUBHEADING
        is InsuranceModel.TerminatedContracts -> R.layout.insurance_terminated_contracts
        InsuranceModel.Error -> R.layout.generic_error
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), retry, marketManager)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        val itemView = holder.itemView
        if (itemView is ComposeView) {
            itemView.disposeComposition()
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: InsuranceModel,
            retry: () -> Unit,
            marketManager: MarketManager
        )

        class CrossSellViewHolder(
            private val composeView: ComposeView,
            private val onClickCrossSell: (CrossSellData.Action) -> Unit,
        ) : ViewHolder(composeView) {
            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                marketManager: MarketManager
            ) {
                if (data !is InsuranceModel.CrossSellCard) {
                    return invalid(data)
                }

                composeView.setContent {
                    val context = LocalContext.current
                    HedvigTheme {
                        CrossSell(
                            data = data.inner,
                            onCardClick = {
                                context.startActivity(CrossSellDetailActivity.newInstance(context, data.inner))
                            },
                            onCtaClick = {
                                onClickCrossSell(data.inner.action)
                            }
                        )
                    }
                }
            }
        }

        class ContractViewHolder(parent: ViewGroup) : ViewHolder(
            parent.inflate(R.layout.insurance_contract_card)
        ) {
            private val binding by viewBinding(InsuranceContractCardBinding::bind)

            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                marketManager: MarketManager
            ) = with(binding) {
                if (data !is InsuranceModel.Contract) {
                    return invalid(data)
                }
                data.contractCardViewState.bindTo(binding, marketManager)
                card.setHapticClickListener {
                    card.transitionName = TRANSITION_NAME
                    card.context.getActivity()?.let { activity ->
                        if (activity is LoggedInActivity) {
                            activity.window.reenterTransition = null
                            activity.window.exitTransition = null
                        }
                        card.context.startActivity(
                            ContractDetailActivity.newInstance(
                                card.context,
                                data.contractCardViewState.id
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
                marketManager: MarketManager
            ) = Unit
        }

        class Error(
            parent: ViewGroup,
        ) : ViewHolder(parent.inflate(R.layout.generic_error)) {
            private val binding by viewBinding(GenericErrorBinding::bind)
            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                marketManager: MarketManager
            ) = with(binding) {
                this.retry.setHapticClickListener {
                    retry()
                }
            }
        }

        class SubheadingViewHolder(private val composeView: ComposeView) : ViewHolder(composeView) {

            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                marketManager: MarketManager
            ) {
                if (data !is InsuranceModel.TerminatedContractsHeader) {
                    return invalid(data)
                }
                composeView.setContent {
                    HedvigTheme {
                        Subheading(stringResource(R.string.insurances_tab_more_title))
                    }
                }
            }
        }

        class NotificationSubheadingViewHolder(composeView: ComposeView) : ViewHolder(composeView) {

            private var data by mutableStateOf<InsuranceModel.CrossSellHeader?>(null)

            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                composeView.setContent {
                    val data = data ?: return@setContent
                    HedvigTheme {
                        NotificationSubheading(
                            text = stringResource(R.string.insurance_tab_cross_sells_title),
                            showNotification = data.showNotificationBadge
                        )
                    }
                }
            }

            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                marketManager: MarketManager
            ) {
                if (data !is InsuranceModel.CrossSellHeader) {
                    return invalid(data)
                }
                this.data = data
            }
        }

        class TerminatedContracts(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.insurance_terminated_contracts)) {
            private val binding by viewBinding(InsuranceTerminatedContractsBinding::bind)
            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                marketManager: MarketManager
            ) = with(binding) {
                if (data !is InsuranceModel.TerminatedContracts) {
                    return invalid(data)
                }

                caption.text = caption.resources.getQuantityString(
                    R.plurals.insurances_tab_terminated_insurance_subtitile,
                    data.quantity,
                    data.quantity
                )
                root.setHapticClickListener {
                    root.context.getActivity()?.let { activity ->
                        activity.window.exitTransition =
                            MaterialSharedAxis(MaterialSharedAxis.X, true)
                        activity.window.reenterTransition =
                            MaterialSharedAxis(MaterialSharedAxis.X, false)
                        root.context.startActivity(
                            TerminatedContractsActivity.newInstance(root.context),
                            ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle()
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val TRANSITION_NAME = "contract_card"

        private const val CROSS_SELL = 1
        private const val SUBHEADING = 2
        private const val NOTIFICATION_SUBHEADING = 3

        object InsuranceAdapterDiffUtilItemCallback : DiffUtil.ItemCallback<InsuranceModel>() {
            override fun areItemsTheSame(oldItem: InsuranceModel, newItem: InsuranceModel): Boolean {
                if (oldItem is InsuranceModel.CrossSellHeader && newItem is InsuranceModel.CrossSellHeader) {
                    // Only a single CrossSellHeader must appear in the list, therefore always true
                    return true
                }
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: InsuranceModel,
                newItem: InsuranceModel
            ): Boolean = oldItem == newItem
        }
    }
}
