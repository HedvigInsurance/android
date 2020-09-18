package com.hedvig.app.ui.decoration

import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CenterItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val edgeMargin = (parent.measuredWidth - view.layoutParams.width) / 2
        outRect.left = edgeMargin
        outRect.right = edgeMargin
    }
}
