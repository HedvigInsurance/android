package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF

class CoverageItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val item = (parent.adapter as? CoverageAdapter)?.currentList?.getOrNull(position) ?: return

        if (item is CoverageModel.Header.InsurableLimits) {
            outRect.top = BASE_MARGIN_DOUBLE
            return
        }

        if (item is CoverageModel.Peril) {
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
    }

    companion object {
        private const val SPAN_LEFT = 0
        private const val SPAN_RIGHT = 1
    }
}
