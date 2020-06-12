package com.hedvig.app.feature.referrals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.fragment.ReferralFragment
import com.hedvig.app.R
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import e
import kotlinx.android.synthetic.main.referrals_code.view.*
import kotlinx.android.synthetic.main.referrals_header.view.*
import kotlinx.android.synthetic.main.referrals_header.view.placeholders
import kotlinx.android.synthetic.main.referrals_row.view.*

class ReferralsAdapter : RecyclerView.Adapter<ReferralsAdapter.ViewHolder>() {
    var items: List<ReferralsModel> = listOf(
        ReferralsModel.Header.LoadingHeader,
        ReferralsModel.Code.LoadingCode,
        ReferralsModel.InvitesHeader,
        ReferralsModel.Referral.LoadingReferral
    )
        set(value) {
            val diff = DiffUtil.calculateDiff(ReferralsDiffCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ReferralsModel.Header -> R.layout.referrals_header
        is ReferralsModel.Code -> R.layout.referrals_code
        ReferralsModel.InvitesHeader -> R.layout.referrals_invites_header
        is ReferralsModel.Referral -> R.layout.referrals_row
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.referrals_header -> ViewHolder.HeaderViewHolder(parent)
        R.layout.referrals_code -> ViewHolder.CodeViewHolder(parent)
        R.layout.referrals_invites_header -> ViewHolder.InvitesHeaderViewHolder(parent)
        R.layout.referrals_row -> ViewHolder.ReferralViewHolder(parent)
        else -> throw Error("Invalid viewType")
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: ReferralsModel)

        class HeaderViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_header, parent, false)
        ) {
            private val emptyTexts = itemView.emptyTexts
            private val nonEmptyTexts = itemView.nonEmptyTexts
            private val placeholders = itemView.placeholders
            private val loadedData = itemView.loadedData

            override fun bind(data: ReferralsModel) {
                when (data) {
                    ReferralsModel.Header.LoadingHeader -> {
                        emptyTexts.remove()
                        nonEmptyTexts.show()
                        placeholders.show()
                        loadedData.remove()
                    }
                    ReferralsModel.Header.LoadedEmptyHeader -> {
                        placeholders.remove()
                        emptyTexts.show()
                        loadedData.remove()
                        nonEmptyTexts.remove()
                    }
                    is ReferralsModel.Header.LoadedHeader -> {
                        placeholders.remove()
                        emptyTexts.remove()
                        nonEmptyTexts.show()
                        loadedData.show()
                        // TODO: Show some numbers when we have them
                    }
                    else -> {
                        e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                    }
                }
            }
        }

        class CodeViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_code, parent, false)
        ) {
            private val placeholder = itemView.codePlaceholder
            private val code = itemView.code
            override fun bind(data: ReferralsModel) {
                when (data) {
                    ReferralsModel.Code.LoadingCode -> {
                        placeholder.show()
                        code.remove()
                    }
                    is ReferralsModel.Code.LoadedCode -> {
                        placeholder.remove()
                        code.show()
                        code.text = data.code
                    }
                    else -> {
                        e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                    }
                }
            }
        }

        class InvitesHeaderViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_invites_header, parent, false)
        ) {
            override fun bind(data: ReferralsModel) = Unit
        }

        class ReferralViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_row, parent, false)
        ) {
            private val placeholders = itemView.placeholders
            private val texts = itemView.texts
            private val name = itemView.name
            private val refereeLabel = itemView.refereeLabel
            private val icon = itemView.icon
            private val status = itemView.status

            override fun bind(data: ReferralsModel) {
                when (data) {
                    ReferralsModel.Referral.LoadingReferral -> {
                        placeholders.show()
                        texts.remove()
                        icon.remove()
                        status.remove()
                    }
                    is ReferralsModel.Referral.Referee -> {
                        bindReferral(data.inner)
                        refereeLabel.show()
                    }
                    is ReferralsModel.Referral.LoadedReferral -> {
                        bindReferral(data.inner)
                        refereeLabel.remove()
                    }
                    else -> {
                        e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                    }
                }
            }

            private fun bindReferral(data: ReferralFragment) {
                placeholders.remove()
                texts.show()
                data.name?.let { name.text = it }
                icon.show()
                status.show()
                data.asActiveReferral?.let { activeReferral ->
                    icon.setImageResource(R.drawable.ic_basketball)
                    status.background =
                        status.context.compatDrawable(R.drawable.background_slightly_rounded_corners)
                            ?.apply {
                                mutate().compatSetTint(status.context.colorAttr(R.attr.colorSurface))
                            }
                    val discountAsNegative =
                        activeReferral.discount.fragments.monetaryAmountFragment.toMonetaryAmount()
                            .negate()
                    status.text = discountAsNegative.format(status.context)
                }
                data.asInProgressReferral?.let {
                    icon.setImageResource(R.drawable.ic_clock_colorless)
                    status.background = null
                    status.text =
                        status.context.getString(R.string.referalls_invitee_states_awaiting___)
                }
                data.asTerminatedReferral?.let {
                    icon.setImageResource(R.drawable.ic_terminated_colorless)
                    status.background = null
                    status.text =
                        status.context.getString(R.string.referalls_invitee_states_terminated)
                }
            }

            companion object {
                private val ReferralFragment.name: String?
                    get() {
                        asActiveReferral?.name?.let { return it }
                        asInProgressReferral?.name?.let { return it }
                        asTerminatedReferral?.name?.let { return it }

                        return null
                    }
            }
        }
    }
}

sealed class ReferralsModel {
    sealed class Header : ReferralsModel() {
        object LoadingHeader : Header()
        object LoadedEmptyHeader : Header()
        data class LoadedHeader(
            private val todo: Unit
        ) : Header()
    }

    sealed class Code : ReferralsModel() {
        object LoadingCode : Code()
        data class LoadedCode(
            val code: String
        ) : Code()
    }

    object InvitesHeader : ReferralsModel()

    sealed class Referral : ReferralsModel() {
        object LoadingReferral : Referral()

        data class Referee(
            val inner: ReferralFragment
        ) : Referral()

        data class LoadedReferral(
            val inner: ReferralFragment
        ) : Referral()
    }
}

class ReferralsDiffCallback(
    private val old: List<ReferralsModel>,
    private val new: List<ReferralsModel>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]

        if (oldItem is ReferralsModel.Header && newItem is ReferralsModel.Header) {
            return true
        }

        if (oldItem is ReferralsModel.Code && newItem is ReferralsModel.Code) {
            return true
        }

        if (oldItem is ReferralsModel.InvitesHeader && newItem is ReferralsModel.InvitesHeader) {
            return true
        }

        return oldItem == newItem
    }

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition] == new[newItemPosition]
}
