package com.hedvig.app.feature.home.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.BASE_MARGIN_SEPTUPLE
import com.hedvig.app.R
import com.hedvig.app.util.extensions.children

class HomeItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val divider = ContextCompat.getDrawable(context, R.drawable.divider)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val item = (parent.adapter as? HomeAdapter)?.currentList?.getOrNull(position) ?: return

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

        if (item is HomeModel.ConnectPayin) {
            val prev = (parent.adapter as? HomeAdapter)?.currentList?.getOrNull(position - 1) ?: return
            if (prev is HomeModel.ConnectPayin) {
                outRect.top = BASE_MARGIN
            }

            if (prev is HomeModel.StartClaimContained) {
                outRect.top = BASE_MARGIN_SEPTUPLE
            }
        }

        if (item is HomeModel.BigText) {
            val prev = (parent.adapter as? HomeAdapter)?.currentList?.getOrNull(position - 1) ?: return
            if (prev is HomeModel.PSA) {
                outRect.top = BASE_MARGIN_DOUBLE
            }
        }

        if (item is HomeModel.PSA) {
            divider?.let { divider ->
                outRect.bottom = divider.intrinsicHeight
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = (parent.adapter as? HomeAdapter)
        parent.children.forEach { view ->
            val position = parent.getChildAdapterPosition(view)
            val item = adapter?.currentList?.getOrNull(position) ?: return

            val prev = (parent.adapter as? HomeAdapter)?.currentList?.getOrNull(position - 1) ?: return
            if (prev is HomeModel.PSA && item is HomeModel.PSA) {
                divider?.draw(c)
            }
        }
    }

    companion object {
        private const val SPAN_LEFT = 0
        private const val SPAN_RIGHT = 1
    }
}
