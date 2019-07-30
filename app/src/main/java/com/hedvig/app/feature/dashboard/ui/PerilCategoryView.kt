package com.hedvig.app.feature.dashboard.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.hedvig.app.R
import com.hedvig.app.ui.view.HedvigCardView
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.animateCollapse
import com.hedvig.app.util.extensions.view.animateExpand
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.peril_category_view.view.*

class PerilCategoryView : HedvigCardView {
    private var attributeSet: AttributeSet? = null
    private var defStyle: Int = 0

    private val iconSize: Int by lazy { resources.getDimensionPixelSize(R.dimen.dashboard_icon) }

    private val halfMargin: Int by lazy { resources.getDimensionPixelSize(R.dimen.base_margin_half) }
    private val tripleMargin: Int by lazy { resources.getDimensionPixelSize(R.dimen.base_margin_triple) }

    var onAnimateExpand: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        inflate(context, R.layout.peril_category_view, this)
        setupAttributes()
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        this.attributeSet = attributeSet
        inflate(context, R.layout.peril_category_view, this)
        setupAttributes()
    }

    constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyle: Int
    ) : super(context, attributeSet, defStyle) {
        this.attributeSet = attributeSet
        this.defStyle = defStyle
        inflate(context, R.layout.peril_category_view, this)
        setupAttributes()
    }

    var categoryIconUrl: Uri? = null
        set(value) {
            field = value

            Glide
                .with(context)
                .load(value)
                .override(iconSize)
                .into(catIcon)
        }

    var categoryIcon: Drawable? = null
        set(value) {
            field = value
            catIcon.setImageDrawable(value)
        }

    var categoryIconId: String? = null
        set(value) {
            field = value

            @DrawableRes val icon = when (value) { // FIXME Oh god this is extremely wack
                "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/du_och_din_familj%402x.png" -> R.drawable.ic_family
                "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/lagenhet%402x.png" -> R.drawable.ic_two_homes
                "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/prylar%402x.png" -> R.drawable.ic_things
                else -> R.drawable.ic_family
            }

            catIcon.setImageDrawable(context.compatDrawable(icon))
        }

    var title: CharSequence? = null
        set(value) {
            field = value
            categoryTitle.text = value
        }

    var subtitle: CharSequence? = null
        set(value) {
            field = value
            categorySubtitle.text = value
        }

    var expandedContent: View? = null
        set(value) {
            field = value
            perilsContainer.removeAllViews()
            perilsContainer.addView(value)

            expandedContentMeasuredHeight = field?.measuredHeight

            if (!toggled) {
                value?.layoutParams?.let { lp ->
                    value.layoutParams = lp.also { it.height = 0 }
                }
            }
        }

    private var expandedContentMeasuredHeight: Int? = null

    val expandedContentContainer: FrameLayout by lazy { perilsContainer }

    private var toggled: Boolean = false

    private fun setupAttributes() {
        resources.getDimensionPixelSize(R.dimen.base_margin).let { setPadding(it, it, it, it) }
        clipToPadding = true

        val attributes = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.PerilCategoryView,
            defStyle,
            0
        )

        attributes.getText(R.styleable.PerilCategoryView_categoryUrl)?.let { url ->
            categoryIconUrl = Uri.parse(url.toString())
        }
        title = attributes.getText(R.styleable.PerilCategoryView_title)
        subtitle = attributes.getText(R.styleable.PerilCategoryView_subtitle)

        attributes.recycle()

        setOnClickListener {
            toggle()
        }
    }

    private fun toggle() {
        if (toggled) {
            expandedContent?.animateCollapse(withOpacity = true)
            expandCollapse
                .animate()
                .withLayer()
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .rotation(0f)
                .start()
            toggled = false
            return
        } else {
            expandedContent?.animateExpand(updateCallback = onAnimateExpand, withOpacity = true)
            expandedContentContainer.show()
            toggled = true
            expandCollapse
                .animate()
                .withLayer()
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .rotation(-180f)
                .start()
        }
    }

    companion object {
        fun build(
            context: Context,
            width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
            height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
            bottomMargin: Int? = null
        ) =
            PerilCategoryView(context)
                .apply {
                    layoutParams = MarginLayoutParams(width, height).also { lp ->
                        lp.topMargin = halfMargin
                        lp.marginStart = tripleMargin
                        lp.marginEnd = tripleMargin
                        lp.bottomMargin = bottomMargin ?: halfMargin
                    }
                }
    }
}
