package com.hedvig.app.feature.keygear.ui.tab

import androidx.recyclerview.widget.DiffUtil
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery

class KeyGearItemsDiffCallback(
    private val old: List<KeyGearItemsQuery.KeyGearItem>,
    private val new: List<KeyGearItemsQuery.KeyGearItem>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition].fragments.keyGearItemFragment.id == new[newItemPosition].fragments.keyGearItemFragment.id

    override fun getOldListSize() = old.size

    override fun getNewListSize() = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition] == new[newItemPosition]
}
