package com.hedvig.app.feature.profile.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import kotlinx.android.synthetic.main.payment_history_item.view.*
import kotlinx.android.synthetic.main.payout_history_header.view.*

class PaymentHistoryAdapter(
    private val items: List<ChargeWrapper>
) : RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {
    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ChargeWrapper.Header -> HEADER
        is ChargeWrapper.Item -> ITEM
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = when (viewType) {
        HEADER -> {
            ViewHolder.HeaderViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.payout_history_header, parent, false)
            )
        }
        ITEM -> ViewHolder.ItemViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.payment_history_item, parent, false)
        )
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
        class HeaderViewHolder(view: View) : ViewHolder(view) {
            val year: TextView = view.header
        }

        class ItemViewHolder(view: View) : ViewHolder(view) {
            val date: TextView = view.date
            val amount: TextView = view.amount
        }
    }

    companion object {
        private const val HEADER = 0
        private const val ITEM = 1
    }
}
