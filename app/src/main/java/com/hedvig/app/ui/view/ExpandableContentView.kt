package com.hedvig.app.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.hedvig.app.R
import com.hedvig.app.databinding.ExpandableContentViewBinding
import com.hedvig.app.util.extensions.view.performOnTapHapticFeedback
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class ExpandableContentView : ConstraintLayout {
    private val binding by viewBinding(ExpandableContentViewBinding::bind)
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

    private var expanded: Boolean = false
        set(value) {
            field = value
            binding.apply {
                expandableContentToggle.setText(
                    if (field) {
                        R.string.OFFER_HOUSE_SUMMARY_BUTTON_MINIMIZE
                    } else {
                        R.string.OFFER_HOUSE_SUMMARY_BUTTON_EXPAND
                    }
                )
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
        }

    init {
        inflate(context, R.layout.expandable_content_view, this)
        hasInflated = true
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (hasInflated) {
            binding.expandableContentContainer.addView(child, index, params)
        } else {
            super.addView(child, index, params)
        }
    }

    fun initialize() {
        contentSizeChanged()
        binding.apply {
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
    }

    fun contentSizeChanged() {
        binding.apply {
            expandableContentContainer.measure(
                MeasureSpec.makeMeasureSpec(root.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(root.height, MeasureSpec.UNSPECIFIED)
            )
            expandedContentHeight = expandableContentContainer.measuredHeight
        }
    }

    companion object {
        const val ANIMATION_DURATION_MILLIS = 300L
        private fun View.updateHeight(newHeight: Int) {
            layoutParams = LayoutParams(
                layoutParams
            ).apply {
                height = newHeight
            }
        }
    }
}
