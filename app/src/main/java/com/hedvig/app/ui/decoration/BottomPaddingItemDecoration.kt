package com.hedvig.app.ui.decoration

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class BottomPaddingItemDecoration(val bottomPadding: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: androidx.recyclerview.widget.RecyclerView,
        state: androidx.recyclerview.widget.RecyclerView.State
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
