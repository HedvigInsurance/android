package com.hedvig.app.feature.profile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show

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

    var name: CharSequence? = null
        set(value) {
            field = value
            findViewById<TextView>(R.id.title).text = field
        }
    var description: CharSequence? = null
        set(value) {
            field = value
            findViewById<TextView>(R.id.caption).apply {
                text = field
                show()
            }
            findViewById<TextView>(R.id.title).gravity = Gravity.NO_GRAVITY
        }

    private fun setupDynamicContent() {
        val attributes =
            context.obtainStyledAttributes(attributeSet, R.styleable.ProfileMenuRow, defStyle, 0)

        name = attributes.getText(R.styleable.ProfileMenuRow_name)

        val description = attributes.getText(R.styleable.ProfileMenuRow_description)
        description?.let { d ->
            findViewById<TextView>(R.id.caption).text = d
        } ?: makeSingleLine()

        attributes.recycle()
    }

    private fun makeSingleLine() {
        findViewById<TextView>(R.id.caption).remove()
        findViewById<TextView>(R.id.title).gravity = Gravity.CENTER_VERTICAL
    }
}
