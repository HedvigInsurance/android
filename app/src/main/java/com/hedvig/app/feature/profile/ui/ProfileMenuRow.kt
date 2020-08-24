package com.hedvig.app.feature.profile.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.constraintlayout.widget.ConstraintLayout
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.profile_row.view.*

class ProfileMenuRow : ConstraintLayout {
    private var attributeSet: AttributeSet? = null
    private var defStyle: Int = 0

    constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyle: Int
    ) : super(context, attributeSet, defStyle) {
        this.attributeSet = attributeSet
        this.defStyle = defStyle
        inflate(context, R.layout.profile_row, this)
        setupDynamicContent()
    }

    constructor(
        context: Context,
        attributeSet: AttributeSet?
    ) : super(context, attributeSet) {
        this.attributeSet = attributeSet
        inflate(context, R.layout.profile_row, this)
        setupDynamicContent()
    }

    constructor(context: Context) : super(context) {
        inflate(context, R.layout.profile_row, this)
        setupDynamicContent()
    }

    var icon: Drawable? = null
        set(value) {
            field = value
            //icon.setImageDrawable(field)
        }
    var name: CharSequence? = null
        set(value) {
            field = value
            title.text = field
        }
    var description: CharSequence? = null
        set(value) {
            field = value
            caption.text = field
            caption.show()
            title.gravity = Gravity.NO_GRAVITY
        }

    private fun setupDynamicContent() {
        val attributes =
            context.obtainStyledAttributes(attributeSet, R.styleable.ProfileMenuRow, defStyle, 0)

        val iconResourceId = attributes.getResourceId(R.styleable.ProfileMenuRow_iconImage, -1)
        if (iconResourceId != -1) icon = context.compatDrawable(iconResourceId)
        name = attributes.getText(R.styleable.ProfileMenuRow_name)

        val description = attributes.getText(R.styleable.ProfileMenuRow_description)
        description?.let { d ->
            caption.text = d
        } ?: makeSingleLine()

        attributes.recycle()
    }

    private fun makeSingleLine() {
        caption.remove()
        title.gravity = Gravity.CENTER_VERTICAL
    }
}
