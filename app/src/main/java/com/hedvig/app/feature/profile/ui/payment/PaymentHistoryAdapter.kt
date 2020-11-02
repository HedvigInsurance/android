package com.hedvig.app.feature.profile.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PaymentHistoryItemBinding
import com.hedvig.app.databinding.PayoutHistoryHeaderBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.viewBinding

class PaymentHistoryAdapter :
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
        when (holder) {
            is ViewHolder.HeaderViewHolder -> {
                (getItem(position) as? ChargeWrapper.Header)?.let { holder.bind(it) }
            }
            is ViewHolder.ItemViewHolder -> {
                (getItem(position) as? ChargeWrapper.Item)?.let { holder.bind(it) }
            }
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class TitleViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.payout_history_title, parent, false)
        ) {
        }

        class HeaderViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.payout_history_header, parent, false)
        ) {
            private val binding by viewBinding(PayoutHistoryHeaderBinding::bind)

            fun bind(item: ChargeWrapper.Header) {
                binding.header.text = item.year.toString()
            }
        }

        class ItemViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.payment_history_item, parent, false)
        ) {
            private val binding by viewBinding(PaymentHistoryItemBinding::bind)
            fun bind(item: ChargeWrapper.Item) {
                binding.apply {
                    date.text = item.charge.date.format(PaymentActivity.DATE_FORMAT)
                    amount.text = amount.context.getString(
                        R.string.PAYMENT_HISTORY_AMOUNT,
                        item.charge.amount.amount.toBigDecimal().toInt()
                    )
                }
            }
        }
    }
}
