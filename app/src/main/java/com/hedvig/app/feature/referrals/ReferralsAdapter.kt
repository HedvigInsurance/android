package com.hedvig.app.feature.referrals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import e
import kotlinx.android.synthetic.main.referrals_code.view.*
import kotlinx.android.synthetic.main.referrals_header.view.*

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
            override fun bind(data: ReferralsModel) {
                when (data) {
                    ReferralsModel.Referral.LoadingReferral -> {

                    }
                    is ReferralsModel.Referral.Referee -> {

                    }
                    else -> {
                        e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
                    }
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
            private val todo: Void
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
