package com.hedvig.app.feature.chat

import android.app.Activity
import android.content.Context
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import com.hedvig.android.owldroid.fragment.ChatMessageFragment
import com.hedvig.android.owldroid.type.KeyboardType
import com.hedvig.app.R
import com.hedvig.app.util.extensions.children
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.view.*
import kotlinx.android.synthetic.main.chat_input_view.view.*

class ChatInputView : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(context, attributeSet, defStyle)

    private lateinit var sendTextMessage: ((String) -> Unit)
    private lateinit var sendSingleSelect: ((String) -> Unit)
    private lateinit var singleSelectLink: ((String) -> Unit)
    private lateinit var openAttachFile: (() -> Unit)

    private var currentlyDisplaying: ChatInputType = NullInput

    private val layoutInflater: LayoutInflater by lazy {
        LayoutInflater.from(context)
    }

    var message: ChatInputType? = null
        set(value) {
            field = value
            value?.let { show(it) }
            when (value) {
                is TextInput -> bindTextInput(value)
                is SingleSelect -> bindSingleSelect(value)
                is ParagraphInput -> bindParagraphInput()
            }
        }

    init {
        inflate(context, R.layout.chat_input_view, this)
        inputText.sendClickListener = {
            performTextMessageSend()
        }
        inputText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                performTextMessageSend()
                return@setOnEditorActionListener true
            }
            if (actionId == EditorInfo.IME_NULL && event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                performTextMessageSend()
                return@setOnEditorActionListener true
            }
            true
        }
        uploadFile.setHapticClickListener {
            inputText.clearFocus()
            openAttachFile()
        }

        hideAllViews()
    }

    private fun hideAllViews() {
        textInputContainer.remove()
        singleSelectContainer.remove()
        paragraphView.remove()
        audioRecorder.remove()
    }

    fun initialize(
        sendTextMessage: (String) -> Unit,
        sendSingleSelect: (String) -> Unit,
        sendSingleSelectLink: (String) -> Unit,
        openAttachFile: () -> Unit,
        requestAudioPermission: () -> Unit,
        uploadRecording: (String) -> Unit
    ) {
        this.sendTextMessage = sendTextMessage
        this.sendSingleSelect = sendSingleSelect
        this.singleSelectLink = sendSingleSelectLink
        this.openAttachFile = openAttachFile
        audioRecorder.initialize(requestAudioPermission, uploadRecording)
    }

    fun clearInput() {
        inputText.text.clear()
    }

    fun audioRecorderPermissionGranted() = audioRecorder.permissionGranted()

    private fun fadOutCurrent(fadeIn: () -> Unit) {
        when (currentlyDisplaying) {
            is TextInput -> textInputContainer.fadeOut(fadeIn)
            is SingleSelect -> singleSelectContainer.fadeOut(fadeIn)
            is ParagraphInput -> paragraphView.fadeOut(fadeIn)
            is Audio -> audioRecorder.fadeOut(fadeIn)
            is NullInput -> fadeIn()
        }
    }

    private fun show(value: ChatInputType) {
        if (value::class != currentlyDisplaying::class) {
            fadOutCurrent {
                when (value) {
                    is TextInput -> textInputContainer.fadeIn()
                    is SingleSelect -> singleSelectContainer.fadeIn()
                    is ParagraphInput -> paragraphView.fadeIn()
                    is Audio -> audioRecorder.fadeIn()
                }
            }
            currentlyDisplaying = value
        }
    }

    private fun bindTextInput(input: TextInput) {
        if (input.richTextSupport) {
            uploadFile.show()
            //sendGif.show()
        } else {
            uploadFile.remove()
            //sendGif.remove()
        }
        inputText.hint = input.hint ?: ""
        inputText.inputType = when (input.keyboardType) {
            KeyboardType.DEFAULT -> InputType.TYPE_CLASS_TEXT
            KeyboardType.EMAIL -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            KeyboardType.PHONE -> InputType.TYPE_CLASS_PHONE
            KeyboardType.NUMBERPAD, KeyboardType.NUMERIC -> InputType.TYPE_CLASS_NUMBER
            KeyboardType.DECIMALPAD -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            else -> InputType.TYPE_CLASS_TEXT
        }
        inputText.requestFocus()
    }

    private fun bindSingleSelect(input: SingleSelect) {
        singleSelectContainer.removeAllViews()
        input.options.forEach {
            when (it) {
                is ChatMessageFragment.AsMessageBodyChoicesSelection ->
                    inflateSingleSelectButton(it.text, it.value, SingleSelectChoiceType.SELECTION)
                is ChatMessageFragment.AsMessageBodyChoicesLink ->
                    inflateSingleSelectButton(it.text, it.value, SingleSelectChoiceType.LINK)
                is ChatMessageFragment.AsMessageBodyChoicesUndefined ->
                    inflateSingleSelectButton(it.text, it.value, SingleSelectChoiceType.UNDEFINED)
            }
        }
    }

    private fun inflateSingleSelectButton(label: String, value: String, type: SingleSelectChoiceType) {
        val singleSelectButton =
            layoutInflater.inflate(R.layout.chat_single_select_button, singleSelectContainer, false) as TextView
        singleSelectButton.text = label
        singleSelectButton.setHapticClickListener {
            singleSelectButton.isSelected = true
            singleSelectButton.setTextColor(context.compatColor(R.color.white))
            disableSingleButtons()
            when (type) {
                SingleSelectChoiceType.UNDEFINED, // TODO: Let's talk about this one
                SingleSelectChoiceType.SELECTION -> sendSingleSelect(value)
                SingleSelectChoiceType.LINK -> singleSelectLink(value)
            }

        }

        singleSelectContainer.addView(singleSelectButton)
    }

    private fun disableSingleButtons() {
        singleSelectContainer.children.forEach { it.isEnabled = false }
    }

    private fun bindParagraphInput() {
        paragraphView.playAnimation()
    }

    fun rotateFileUploadIcon(isOpening: Boolean) {
        SpringAnimation(uploadFile, DynamicAnimation.ROTATION).animateToFinalPosition(if (isOpening) 135f else 0f)
    }

    private fun performTextMessageSend() {
        sendTextMessage(inputText.currentMessage)
        dismissKeyboard()
    }

    private fun dismissKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

