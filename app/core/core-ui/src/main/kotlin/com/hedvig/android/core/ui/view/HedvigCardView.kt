package com.hedvig.android.core.ui.view

import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.hedvig.android.core.ui.R

class HedvigCardView @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {
  init {
    background = AppCompatResources.getDrawable(context, R.drawable.hedvig_card_view_ripple)
    outlineProvider = OutlineProvider()
  }

  inner class OutlineProvider : ViewOutlineProvider() {
    private val inset: Rect = Rect()

    override fun getOutline(view: View?, outline: Outline?) {
      view?.background?.copyBounds(inset)

      inset.left += 16.viewDps
      inset.right -= 16.viewDps
      inset.top += 16.viewDps
      inset.bottom -= 12.viewDps

      outline?.setRoundRect(inset, 8f.viewDps)
    }
  }
}
