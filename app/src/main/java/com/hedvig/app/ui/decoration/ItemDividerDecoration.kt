package com.hedvig.app.ui.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R

class ItemDividerDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val divider = ContextCompat.getDrawable(context, R.drawable.divider)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        parent.adapter?.let { adapter ->
            val childrenPosition = parent.getChildAdapterPosition(view).let { position ->
                if (position == RecyclerView.NO_POSITION)
                    return
                else position
            }
            divider?.let { divider ->
                outRect.bottom = when (adapter.getItemViewType(childrenPosition)) {
                    R.layout.psa_box -> divider.intrinsicWidth
                    else -> 0
                }
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.adapter?.let { adapter ->
            parent.children.forEach { view ->
                val childAdapterPosition = parent.getChildAdapterPosition(view)
                    .let { if (it == RecyclerView.NO_POSITION) return else it }

                divider?.let { divider ->
                    when (adapter.getItemViewType(childAdapterPosition)) {
                        R.layout.psa_box -> divider.drawSeparator(view, parent, c)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun Drawable.drawSeparator(view: View, parent: RecyclerView, canvas: Canvas) =
        apply {
            val left = view.right
            val top = parent.paddingTop
            val right = left + intrinsicWidth
            val bottom = top + intrinsicHeight - parent.paddingBottom
            bounds = Rect(left, top, right, bottom)
            draw(canvas)
        }
}
