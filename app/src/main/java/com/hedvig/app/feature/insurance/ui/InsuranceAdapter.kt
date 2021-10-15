package com.hedvig.app.feature.insurance.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.GenericErrorBinding
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.databinding.InsuranceTerminatedContractsBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.getActivity
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class InsuranceAdapter(
    private val tracker: InsuranceTracker,
    private val marketManager: MarketManager,
    private val retry: () -> Unit
) : ListAdapter<InsuranceModel, InsuranceAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.insurance_contract_card -> ViewHolder.ContractViewHolder(parent)
        CROSS_SELL -> ViewHolder.CrossSellViewHolder(ComposeView(parent.context))
        R.layout.insurance_header -> ViewHolder.TitleViewHolder(parent)
        R.layout.generic_error -> ViewHolder.Error(parent)
        SUBHEADING -> ViewHolder.SubheadingViewHolder(ComposeView(parent.context))
        R.layout.insurance_terminated_contracts -> ViewHolder.TerminatedContracts(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), retry, tracker, marketManager)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is InsuranceModel.Contract -> R.layout.insurance_contract_card
        is InsuranceModel.CrossSell -> CROSS_SELL
        is InsuranceModel.Header -> R.layout.insurance_header
        InsuranceModel.TerminatedContractsHeader,
        is InsuranceModel.CrossSellHeader -> SUBHEADING
        is InsuranceModel.TerminatedContracts -> R.layout.insurance_terminated_contracts
        InsuranceModel.Error -> R.layout.generic_error
    }

    override fun onViewRecycled(holder: ViewHolder) {
        if (holder.itemView is ComposeView) {
            holder.itemView.disposeComposition()
        }
        if (holder is ViewHolder.SubheadingViewHolder) {
            holder.composeView.disposeComposition()
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: InsuranceModel,
            retry: () -> Unit,
            tracker: InsuranceTracker,
            marketManager: MarketManager
        )

        fun invalid(data: InsuranceModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class CrossSellViewHolder(private val composeView: ComposeView) : ViewHolder(composeView) {
            init {
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }

            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                tracker: InsuranceTracker,
                marketManager: MarketManager
            ) {
                if (data !is InsuranceModel.CrossSell) {
                    return invalid(data)
                }

                composeView.setContent {
                    val context = LocalContext.current
                    HedvigTheme {
                        CrossSell(
                            data = data,
                            onCtaClick = {
                                when (val action = data.action) {
                                    InsuranceModel.CrossSell.Action.Chat -> openChat(context)
                                    is InsuranceModel.CrossSell.Action.Embark ->
                                        openEmbark(context, action.embarkStoryId, data.title)
                                }
                            }
                        )
                    }
                }
            }

            private fun openChat(context: Context) {
                val intent = ChatActivity.newInstance(context, true)
                val options =
                    ActivityOptionsCompat.makeCustomAnimation(
                        context,
                        R.anim.chat_slide_up_in,
                        R.anim.stay_in_place
                    )

                ActivityCompat.startActivity(context, intent, options.toBundle())
            }

            private fun openEmbark(context: Context, embarkStoryId: String, title: String) {
                context.startActivity(
                    EmbarkActivity.newInstance(context, embarkStoryId, title)
                )
            }
        }

        class ContractViewHolder(parent: ViewGroup) : ViewHolder(
            parent.inflate(R.layout.insurance_contract_card)
        ) {
            private val binding by viewBinding(InsuranceContractCardBinding::bind)

            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                tracker: InsuranceTracker,
                marketManager: MarketManager
            ) = with(binding) {
                if (data !is InsuranceModel.Contract) {
                    return invalid(data)
                }
                data.inner.bindTo(binding, marketManager)
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
                tracker: InsuranceTracker,
                marketManager: MarketManager
            ) = Unit
        }

        class Error(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.generic_error)) {
            private val binding by viewBinding(GenericErrorBinding::bind)
            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                tracker: InsuranceTracker,
                marketManager: MarketManager
            ) = with(binding) {
                this.retry.setHapticClickListener {
                    tracker.retry()
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
                tracker: InsuranceTracker,
                marketManager: MarketManager
            ) {
                if (data !is InsuranceModel.TerminatedContractsHeader && data !is InsuranceModel.CrossSellHeader) {
                    return invalid(data)
                }
                val showNotificationDot = (data is InsuranceModel.CrossSellHeader && data.showNotificationBadge)
                composeView.setContent {
                    val subheadingText = when (data) {
                        is InsuranceModel.CrossSellHeader ->
                            stringResource(R.string.insurance_tab_cross_sells_title)
                        InsuranceModel.TerminatedContractsHeader ->
                            stringResource(R.string.insurances_tab_more_title)
                        else -> ""
                    }

                    HedvigTheme {
                        if (showNotificationDot) {
                            NotificationSubheading(subheadingText)
                        } else {
                            Subheading(subheadingText)
                        }
                    }
                }
            }
        }

        class TerminatedContracts(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.insurance_terminated_contracts)) {
            private val binding by viewBinding(InsuranceTerminatedContractsBinding::bind)
            override fun bind(
                data: InsuranceModel,
                retry: () -> Unit,
                tracker: InsuranceTracker,
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
    }
}
