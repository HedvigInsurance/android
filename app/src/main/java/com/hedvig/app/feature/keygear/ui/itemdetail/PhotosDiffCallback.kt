package com.hedvig.app.feature.keygear.ui.itemdetail

import androidx.recyclerview.widget.DiffUtil

class PhotosDiffCallback(
    private val old: List<String?>,
    private val new: List<String?>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition] == new[newItemPosition]

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areItemsTheSame(oldItemPosition, newItemPosition)
}
