package com.hedvig.app.feature.profile.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import kotlinx.android.synthetic.main.payment_history_item.view.*
import kotlinx.android.synthetic.main.payout_history_header.view.*
import kotlinx.android.synthetic.main.payout_history_title.view.*

class PaymentHistoryAdapter(
) : RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {

    var items: List<ChargeWrapper> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = when (items[position]) {
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
                (items[position] as? ChargeWrapper.Header)?.let { item ->
                    holder.year.text = item.year.toString()
                }
            }
            is ViewHolder.ItemViewHolder -> {
                (items[position] as? ChargeWrapper.Item)?.let { item ->
                    holder.date.text = item.charge.date.format(PaymentActivity.DATE_FORMAT)
                    holder.amount.text = holder.amount.context.getString(
                        R.string.PAYMENT_HISTORY_AMOUNT,
                        item.charge.amount.amount.toBigDecimal().toInt()
                    )
                }
            }
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class TitleViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.payout_history_title, parent, false)
        ) {
            val title: TextView = itemView.title
        }

        class HeaderViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.payout_history_header, parent, false)
        ) {
            val year: TextView = itemView.header
        }

        class ItemViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.payment_history_item, parent, false)
        ) {
            val date: TextView = itemView.date
            val amount: TextView = itemView.amount
        }
    }
}
