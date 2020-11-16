package com.hedvig.app.feature.profile.ui.payment

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import java.time.LocalDate

class PaymentAdapter :
    ListAdapter<PaymentModel, PaymentAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.payment_title -> ViewHolder.Title(parent)
        R.layout.payment_card -> ViewHolder.Card(parent)
        R.layout.payment_price -> ViewHolder.Price(parent)
        R.layout.payment_history -> ViewHolder.History(parent)
        R.layout.payment_header -> ViewHolder.Header(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        PaymentModel.Title -> R.layout.payment_title
        is PaymentModel.Card -> R.layout.payment_card
        is PaymentModel.Price -> R.layout.payment_price
        PaymentModel.PaymentHistory -> R.layout.payment_history
        PaymentModel.Header -> R.layout.payment_header
        is PaymentModel.CardInfo -> R.layout.card_info
        PaymentModel.ChangePaymentMethod -> TODO()
        PaymentModel.AddPayoutAccount -> TODO()
        is PaymentModel.Paragraph -> TODO()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: PaymentModel)

        class Title(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_title)) {
            override fun bind(item: PaymentModel) {
            }
        }

        class Card(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_card)) {
            override fun bind(item: PaymentModel) {
            }
        }

        class Price(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_price)) {
            override fun bind(item: PaymentModel) {
            }
        }

        class History(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_history)) {
            override fun bind(item: PaymentModel) {
            }
        }

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.payment_header)) {
            override fun bind(item: PaymentModel) {
            }
        }
    }
}

sealed class PaymentModel {
    object Title : PaymentModel()
    data class Card(
        val fullPrice: Int,
        val discountedPrice: Int,
        val date: LocalDate
    ) : PaymentModel()

    data class Price(val price: Int) : PaymentModel()
    object PaymentHistory : PaymentModel()
    object Header : PaymentModel()
    data class CardInfo(val cardType: String, val CardNumber: String) : PaymentModel()
    object ChangePaymentMethod : PaymentModel()
    object AddPayoutAccount : PaymentModel()
    data class Paragraph(val text: String) : PaymentModel()
}
