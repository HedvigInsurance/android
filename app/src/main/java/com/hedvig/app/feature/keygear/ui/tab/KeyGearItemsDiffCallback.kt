package com.hedvig.app.feature.keygear.ui.tab

import androidx.recyclerview.widget.DiffUtil
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery

class KeyGearItemsDiffCallback(
    private val old: List<KeyGearItemsQuery.KeyGearItem>,
    private val new: List<KeyGearItemsQuery.KeyGearItem>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = when {
        oldItemPosition == 0 && newItemPosition == 0 -> {
            true
        }
        oldItemPosition == 0 || newItemPosition == 0 -> {
            false
        }
        else -> {
            old[oldItemPosition - 1].fragments.keyGearItemFragment.id == new[newItemPosition - 1].fragments.keyGearItemFragment.id
        }
    }

    override fun getOldListSize() = old.size + 1

    override fun getNewListSize() = new.size + 1

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = when {
        oldItemPosition == 0 && newItemPosition == 0 -> {
            true
        }
        oldItemPosition == 0 || newItemPosition == 0 -> {
            false
        }
        else -> {
            old[oldItemPosition - 1] == new[newItemPosition - 1]
        }
    }
}
