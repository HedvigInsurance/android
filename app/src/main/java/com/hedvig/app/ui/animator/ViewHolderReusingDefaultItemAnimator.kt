package com.hedvig.app.ui.animator

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class ViewHolderReusingDefaultItemAnimator : DefaultItemAnimator() {
    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder) = true
}
