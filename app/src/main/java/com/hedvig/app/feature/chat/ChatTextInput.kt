package com.hedvig.app.feature.chat

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import kotlinx.android.synthetic.main.chat_input_view.view.*


class ChatTextInput : EditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var sendIsDisabled = true

    var sendClickListener: (() -> Unit)? = null

    val currentMessage: String
        get() = text.toString()

    private val sendDrawable: Drawable = compoundDrawables[2]
        ?: throw RuntimeException("No send drawable set, right drawable must be set!")

    private val inputTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s.isNullOrBlank()) {
                disableSendIcon()
            } else {
                enableSendIcon()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }

    init {
        disableSendIcon()
    }

    override fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        right?.let { rightDrawable ->
            val gravityDrawable = BottomRightCompoundDrawableWrapper(rightDrawable, this.paddingEnd, this.paddingBottom)
            rightDrawable.setBounds(0, 0, rightDrawable.intrinsicWidth, rightDrawable.intrinsicHeight)
            gravityDrawable.setBounds(0, 0, rightDrawable.intrinsicWidth, rightDrawable.intrinsicHeight)

            return super.setCompoundDrawables(left, top, gravityDrawable, bottom)
        }
        super.setCompoundDrawables(left, top, right, bottom)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!sendIsDisabled && event.action == MotionEvent.ACTION_DOWN) {
            val tapBounds = Rect(sendDrawable.bounds)
            tapBounds.set(tapBounds.left - EXTRA_TAP_AREA,
                tapBounds.top - EXTRA_TAP_AREA,
                tapBounds.right + EXTRA_TAP_AREA,
                tapBounds.bottom + EXTRA_TAP_AREA)

            val offset = height - sendDrawable.bounds.height()
            if (tapBounds.contains(width - event.x.toInt(), event.y.toInt() - offset)) {
                sendClickListener?.invoke()
                event.action = MotionEvent.ACTION_CANCEL
                return false
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        inputText.addTextChangedListener(inputTextWatcher)
    }

    override fun onDetachedFromWindow() {
        inputText.removeTextChangedListener(inputTextWatcher)
        super.onDetachedFromWindow()
    }

    private fun disableSendIcon() {
        sendDrawable.alpha = DISABLED_ALPHA
        sendIsDisabled = true
    }

    private fun enableSendIcon() {
        sendDrawable.alpha = 255
        sendIsDisabled = false
    }

    companion object {
        private const val EXTRA_TAP_AREA = 30
        private const val DISABLED_ALPHA = 50
    }
}

