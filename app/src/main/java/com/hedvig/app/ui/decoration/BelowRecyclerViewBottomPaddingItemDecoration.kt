package com.hedvig.app.ui.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BelowRecyclerViewBottomPaddingItemDecoration(
    private val bottomPadding: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        val lastPosition = (parent.adapter?.itemCount ?: 0) - 1
        if (position == lastPosition) {
            outRect.bottom = bottomPadding
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }
}
