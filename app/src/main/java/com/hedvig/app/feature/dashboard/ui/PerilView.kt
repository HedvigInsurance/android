package com.hedvig.app.feature.dashboard.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.peril_view.view.*

class PerilView : LinearLayout {
    private var attributeSet: AttributeSet? = null
    private var defStyle: Int = 0

    private val doubleMargin: Int = resources.getDimensionPixelSize(R.dimen.base_margin_double)

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        this.attributeSet = attributeSet
    }

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(context, attributeSet, defStyle) {
        this.attributeSet = attributeSet
        this.defStyle = defStyle
    }

    init {
        inflate(context, R.layout.peril_view, this)
        setupAttributes()
    }

    var perilIcon: Drawable? = null
        set(value) {
            field = value
            image.setImageDrawable(value)
        }

    var perilIconId: String? = null
        set(value) {
            field = value
            value?.let {
                image.setImageDrawable(
                    context.compatDrawable(
                        PerilIcon.from(
                            it
                        )
                    )
                )
            }
        }

    var perilName: CharSequence? = null
        set(value) {
            field = value
            text.text = value
        }

    private fun setupAttributes() {
        orientation = VERTICAL

        val attributes = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.PerilView,
            defStyle,
            0
        )

        perilIcon = attributes.getDrawable(R.styleable.PerilView_perilIcon)
        perilName = attributes.getText(R.styleable.PerilView_perilText)

        attributes.recycle()
    }

    companion object {
        fun build(
            context: Context,
            width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
            height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
            name: String? = null,
            iconId: String? = null,
            onClick: ((View) -> Unit)? = null
        ) = PerilView(context).apply {
            layoutParams = MarginLayoutParams(width, height).also { lp ->
                lp.topMargin = doubleMargin
                lp.marginStart = doubleMargin
                lp.marginEnd = doubleMargin
                name?.let { perilName = it }
                iconId?.let { perilIconId = it }
                onClick?.let {
                    setHapticClickListener(it)
                }
            }
        }
    }
}
