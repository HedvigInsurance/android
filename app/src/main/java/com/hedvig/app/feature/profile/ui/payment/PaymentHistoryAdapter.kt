package com.hedvig.app.feature.profile.ui.payment

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PaymentHistoryItemBinding
import com.hedvig.app.databinding.PayoutHistoryHeaderBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding
import e

class PaymentHistoryAdapter(
    private val marketManager: MarketManager
) :
    ListAdapter<ChargeWrapper, PaymentHistoryAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ChargeWrapper.Title -> R.layout.payout_history_title
        is ChargeWrapper.Header -> R.layout.payout_history_header
        is ChargeWrapper.Item -> R.layout.payment_history_item
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = when (viewType) {
        R.layout.payout_history_title -> ViewHolder.TitleViewHolder(parent)
        R.layout.payout_history_header -> ViewHolder.HeaderViewHolder(parent)
        R.layout.payment_history_item -> ViewHolder.ItemViewHolder(parent)
        else -> {
            throw RuntimeException("Invariant detected: viewType is $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), marketManager)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: ChargeWrapper, marketManager: MarketManager): Any?

        fun invalid(data: ChargeWrapper) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class TitleViewHolder(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.payout_history_title)) {
            override fun bind(data: ChargeWrapper, marketManager: MarketManager) = Unit
        }

        class HeaderViewHolder(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.payout_history_header)) {
            private val binding by viewBinding(PayoutHistoryHeaderBinding::bind)

            override fun bind(data: ChargeWrapper, marketManager: MarketManager) = with(binding) {
                if (data !is ChargeWrapper.Header) {
                    return invalid(data)
                }
                header.text = data.year.toString()
            }
        }

        class ItemViewHolder(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.payment_history_item)) {
            private val binding by viewBinding(PaymentHistoryItemBinding::bind)
            override fun bind(data: ChargeWrapper, marketManager: MarketManager) = with(binding) {
                if (data !is ChargeWrapper.Item) {
                    return invalid(data)
                }
                date.text = data.charge.date.format(PaymentActivity.DATE_FORMAT)
                amount.text =
                    data.charge.amount.fragments.monetaryAmountFragment.toMonetaryAmount()
                        .format(amount.context, marketManager.market)
            }
        }
    }
}
