package com.hedvig.app.util

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

/**
 * Super specialized case of a grid layout manager, that has a spanCount of 2 and lays out the last item at the center
 * of the grid
 */
class CenteredLastItemTwoLineGridLayoutManager(context: Context) : GridLayoutManager(context, spanCount) {
    init {
        spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val itemCount = this@CenteredLastItemTwoLineGridLayoutManager.itemCount
                if (itemCount % 2 == 0) return 1
                val lastIndex = itemCount - 1
                if (position == lastIndex) return spanCount
                return 1
            }
        }
    }

    companion object {
        private const val spanCount: Int = 2
    }
}
