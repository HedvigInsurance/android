package com.hedvig.app.feature.referrals.ui.tab

import androidx.recyclerview.widget.DiffUtil

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
