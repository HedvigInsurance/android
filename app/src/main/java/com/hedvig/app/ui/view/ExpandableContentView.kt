package com.hedvig.app.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.performOnTapHapticFeedback
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.expandable_content_view.view.*

class ExpandableContentView : ConstraintLayout {
    private var hasInflated: Boolean = false
    private var expandedContentHeight: Int = -1

    private val collapsedHeight =
        resources.getDimensionPixelSize(R.dimen.expandable_content_view_collapsed_height)

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    )

    var expanded: Boolean = false
        set(value) {
            field = value
            expandableContentToggle.text = if (field) {
                context.getString(R.string.OFFER_HOUSE_SUMMARY_BUTTON_MINIMIZE)
            } else {
                context.getString(R.string.OFFER_HOUSE_SUMMARY_BUTTON_EXPAND)
            }
            ValueAnimator.ofInt(
                expandableContentContainer.height,
                if (field) {
                    expandedContentHeight
                } else {
                    collapsedHeight
                }
            ).apply {
                interpolator = DecelerateInterpolator()
                duration = ANIMATION_DURATION_MILLIS
                addUpdateListener { va ->
                    expandableContentContainer.updateHeight(va.animatedValue as Int)
                    if (field) {
                        bottomFadeOut.alpha = 1.0f - animatedFraction
                    } else {
                        bottomFadeOut.alpha = animatedFraction
                    }
                }
                start()
            }
        }

    init {
        inflate(context, R.layout.expandable_content_view, this)
        hasInflated = true
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (hasInflated) {
            expandableContentContainer.addView(child, index, params)
        } else {
            super.addView(child, index, params)
        }
    }

    fun initialize() {
        contentSizeChanged()
        expandableContentContainer.updateHeight(collapsedHeight)

        expandableContentToggle.setHapticClickListener {
            expanded = !expanded
        }

        expandableContentContainer.setOnClickListener {
            if (!expanded) {
                performOnTapHapticFeedback()
                expanded = true
            }
        }
    }

    fun contentSizeChanged() {
        expandableContentContainer.measure(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        expandedContentHeight = expandableContentContainer.measuredHeight
    }

    companion object {
        const val ANIMATION_DURATION_MILLIS = 300L
        fun View.updateHeight(newHeight: Int) {
            layoutParams = LayoutParams(
                layoutParams
            ).apply {
                height = newHeight
            }
        }
    }
}
