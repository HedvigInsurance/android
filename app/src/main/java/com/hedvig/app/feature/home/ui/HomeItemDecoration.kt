package com.hedvig.app.feature.home.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.BASE_MARGIN_SEPTUPLE

class HomeItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val item = (parent.adapter as? HomeAdapter)?.items?.getOrNull(position) ?: return

        if (item is HomeModel.CommonClaim) {
            val spanIndex =
                (view.layoutParams as? GridLayoutManager.LayoutParams)?.spanIndex ?: return

            when (spanIndex) {
                SPAN_LEFT -> {
                    outRect.left = BASE_MARGIN_DOUBLE
                    outRect.right = BASE_MARGIN_HALF
                }
                SPAN_RIGHT -> {
                    outRect.left = BASE_MARGIN_HALF
                    outRect.right = BASE_MARGIN_DOUBLE
                }
            }
            return
        }

        if (item is HomeModel.InfoCard) {
            val prev = (parent.adapter as? HomeAdapter)?.items?.getOrNull(position - 1) ?: return
            if (prev is HomeModel.InfoCard) {
                outRect.top = BASE_MARGIN
            }

            if (prev is HomeModel.StartClaimContained) {
                outRect.top = BASE_MARGIN_SEPTUPLE
            }
        }

        if (item is HomeModel.PSA) {
            
        }
    }

    companion object {
        private const val SPAN_LEFT = 0
        private const val SPAN_RIGHT = 1
    }
}
