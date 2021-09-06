package com.hedvig.app.feature.insurance.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.compose.rememberImagePainter
import com.commit451.coiltransformations.CropTransformation
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
import com.hedvig.app.ui.compose.HedvigTheme
import com.hedvig.app.ui.compose.hedvigBlack
import com.hedvig.app.ui.compose.hedvigBlack12percent
import com.hedvig.app.ui.compose.whiteHighEmphasis
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.compose.rememberBlurHash
import com.hedvig.app.util.extensions.getActivity
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class InsuranceAdapter(
    private val tracker: InsuranceTracker,
    private val marketManager: MarketManager,
    private val retry: () -> Unit
) :
    ListAdapter<InsuranceModel, InsuranceAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

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
        InsuranceModel.CrossSellHeader -> SUBHEADING
        is InsuranceModel.TerminatedContracts -> R.layout.insurance_terminated_contracts
        InsuranceModel.Error -> R.layout.generic_error
    }

    override fun onViewRecycled(holder: ViewHolder) {
        if (holder is ViewHolder.CrossSellViewHolder) {
            holder.composeView.disposeComposition()
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
        ): Any?

        fun invalid(data: InsuranceModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class CrossSellViewHolder(val composeView: ComposeView) : ViewHolder(composeView) {
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
                                        openEmbark(context, action.embarkStoryId)
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
                        R.anim.activity_slide_up_in,
                        R.anim.stay_in_place
                    )

                ActivityCompat.startActivity(context, intent, options.toBundle())
            }

            private fun openEmbark(context: Context, embarkStoryId: String) {
                context.startActivity(
                    EmbarkActivity.newInstance(context, embarkStoryId, "")
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
            ): Any? = with(binding) {
                this.retry.setHapticClickListener {
                    tracker.retry()
                    retry()
                }
            }
        }

        class SubheadingViewHolder(val composeView: ComposeView) :
            ViewHolder(composeView) {

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
                composeView.setContent {
                    HedvigTheme {
                        Subheading(
                            when (data) {
                                InsuranceModel.CrossSellHeader ->
                                    stringResource(R.string.insurance_tab_cross_sells_title)
                                InsuranceModel.TerminatedContractsHeader ->
                                    stringResource(R.string.insurances_tab_more_title)
                                else -> ""
                            }
                        )
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

@Composable
fun Subheading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 48.dp,
                end = 16.dp,
                bottom = 8.dp,
            ),
    )
}

@Preview(showBackground = true)
@Composable
fun SubheadingPreview() {
    HedvigTheme {
        Subheading("Add more coverage")
    }
}

/*
 * Note: This Composable uses hardcoded colors due to difficulties with
 * declaring a particular component to be in dark theme instead of the
 * default. When we update `HedvigTheme` to be Compose-first instead of
 * XML-Theme first, we can reconfigure the theme for this composable to
 * be `dark` no matter what the system value is.
 */
@Composable
fun CrossSell(
    data: InsuranceModel.CrossSell,
    onCtaClick: () -> Unit,
) {
    val placeholder by rememberBlurHash(data.backgroundBlurHash, 64, 32)
    Card(
        border = BorderStroke(1.dp, hedvigBlack12percent),
        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            )
            .height(200.dp),
    ) {
        Image(
            painter = rememberImagePainter(
                data = data.backgroundUrl,
                builder = {
                    transformations(CropTransformation())
                    placeholder(placeholder)
                    crossfade(true)
                },
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x00000000),
                            Color(0xFF000000),
                        ),

                    )
                )
                .padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.subtitle1,
                        color = whiteHighEmphasis,
                    )
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.subtitle2,
                        color = whiteHighEmphasis,
                    )
                }
                Button(
                    onClick = onCtaClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = whiteHighEmphasis,
                        contentColor = hedvigBlack,
                    )
                ) {
                    Text(
                        text = data.callToAction,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrossSellPreview() {
    HedvigTheme {
        CrossSell(
            data = InsuranceModel.CrossSell(
                title = "Accident Insurance",
                description = "179 kr/mo.",
                callToAction = "Calculate price",
                action = InsuranceModel.CrossSell.Action.Chat,
                backgroundUrl = "https://images.unsplash.com/photo-1628996796855-0b056a464e06",
                backgroundBlurHash = "LJC6\$2-:DiWB~WxuRkayMwNGo~of",
            ),
            onCtaClick = {}
        )
    }
}
