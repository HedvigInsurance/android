package com.hedvig.app.feature.keygear.ui.createitem

import androidx.recyclerview.widget.DiffUtil

class PhotoDiffCallback(
    private val old: List<Photo>,
    private val new: List<Photo>
) : DiffUtil.Callback() {

    override fun getOldListSize() = old.size

    override fun getNewListSize() = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old.getOrNull(oldItemPosition)?.uri == new.getOrNull(newItemPosition)?.uri

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areItemsTheSame(oldItemPosition, newItemPosition)
}
