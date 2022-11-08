package com.hedvig.app.feature.chat.ui

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.apollo.graphql.type.KeyboardType
import com.hedvig.app.R
import com.hedvig.app.databinding.ChatInputViewBinding
import com.hedvig.app.feature.chat.Audio
import com.hedvig.app.feature.chat.ChatInputType
import com.hedvig.app.feature.chat.NullInput
import com.hedvig.app.feature.chat.ParagraphInput
import com.hedvig.app.feature.chat.SingleSelect
import com.hedvig.app.feature.chat.SingleSelectChoiceType
import com.hedvig.app.feature.chat.TextInput
import com.hedvig.app.util.extensions.avdSetLooping
import com.hedvig.app.util.extensions.avdStart
import com.hedvig.app.util.extensions.avdStop
import com.hedvig.app.util.extensions.children
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.view.dismissKeyboard
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.fadeOut
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding

class ChatInputView : FrameLayout {
  private val binding by viewBinding(ChatInputViewBinding::bind)

  constructor(context: Context) : super(context)
  constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
  constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(
    context,
    attributeSet,
    defStyle,
  )

  private lateinit var sendTextMessage: ((String) -> Unit)
  private lateinit var sendSingleSelect: ((String) -> Unit)
  private lateinit var singleSelectLink: ((String) -> Unit)
  private lateinit var openAttachFile: (() -> Unit)
  private lateinit var openSendGif: (() -> Unit)
  private lateinit var chatRecyclerView: RecyclerView

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
        is Audio, NullInput, null -> {}
      }
    }

  init {
    inflate(context, R.layout.chat_input_view, this)
    binding.apply {
      inputText.sendClickListener = {
        performTextMessageSend()
      }
      inputText.setOnEditorActionListener { _, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_SEND) {
          performTextMessageSend()
          return@setOnEditorActionListener true
        }
        if (
          actionId == EditorInfo.IME_NULL &&
          event.action == KeyEvent.ACTION_UP &&
          event.keyCode == KeyEvent.KEYCODE_ENTER
        ) {
          performTextMessageSend()
          return@setOnEditorActionListener true
        }
        true
      }
      attachFileBackground.setHapticClickListener {
        inputText.clearFocus()
        openAttachFile()
      }
      sendGif.setHapticClickListener {
        inputText.clearFocus()
        openSendGif()
      }
      paragraphView.avdSetLooping()
    }

    hideAllViews()
  }

  private fun hideAllViews() {
    binding.apply {
      textInputContainer.remove()
      singleSelectContainer.remove()
      paragraphView.remove()
    }
  }

  fun initialize(
    sendTextMessage: (String) -> Unit,
    sendSingleSelect: (String) -> Unit,
    sendSingleSelectLink: (String) -> Unit,
    openAttachFile: () -> Unit,
    openSendGif: () -> Unit,
    chatRecyclerView: RecyclerView,
  ) {
    this.sendTextMessage = sendTextMessage
    this.sendSingleSelect = sendSingleSelect
    this.singleSelectLink = sendSingleSelectLink
    this.openAttachFile = openAttachFile
    this.openSendGif = openSendGif
    this.chatRecyclerView = chatRecyclerView
  }

  fun clearInput() {
    binding.inputText.text?.clear()
  }

  private fun fadeOutCurrent(fadeIn: () -> Unit) {
    binding.apply {
      when (currentlyDisplaying) {
        is TextInput -> textInputContainer.fadeOut(fadeIn)
        is SingleSelect -> {
          singleSelectContainer.fadeOut(fadeIn)
        }
        is ParagraphInput -> {
          paragraphView.fadeOut(
            endAction = {
              paragraphView.avdStop()
              fadeIn()
            },
          )
        }
        is Audio, is NullInput -> fadeIn()
      }
    }
  }

  private fun show(value: ChatInputType) {
    binding.apply {
      if (value::class != currentlyDisplaying::class) {
        fadeOutCurrent {
          when (value) {
            is TextInput -> textInputContainer.fadeIn()
            is SingleSelect -> singleSelectContainer.fadeIn()
            is ParagraphInput -> paragraphView.fadeIn()
            is Audio, NullInput -> {}
          }
        }
        currentlyDisplaying = value
      }
    }
  }

  private fun bindTextInput(input: TextInput) {
    binding.apply {
      if (input.richTextSupport) {
        attachFileBackground.show()
        sendGif.show()
      } else {
        attachFileBackground.remove()
        sendGif.remove()
      }
      inputText.hint = input.hint ?: ""
      inputText.inputType = when (input.keyboardType) {
        KeyboardType.DEFAULT -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        KeyboardType.EMAIL -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        KeyboardType.PHONE -> InputType.TYPE_CLASS_PHONE
        KeyboardType.NUMBERPAD, KeyboardType.NUMERIC -> InputType.TYPE_CLASS_NUMBER
        KeyboardType.DECIMALPAD -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        else -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
      }
      inputText.requestFocus()
    }
  }

  private fun bindSingleSelect(input: SingleSelect) {
    binding.singleSelectContainer.removeAllViews()
    input.options.forEach { option ->
      option.asMessageBodyChoicesSelection?.let { selection ->
        inflateSingleSelectButton(
          selection.text,
          selection.value,
          SingleSelectChoiceType.SELECTION,
        )
      }
      option.asMessageBodyChoicesLink?.let { link ->
        inflateSingleSelectButton(link.text, link.value, SingleSelectChoiceType.LINK)
      }
      option.asMessageBodyChoicesUndefined?.let { undefined ->
        inflateSingleSelectButton(
          undefined.text,
          undefined.value,
          SingleSelectChoiceType.UNDEFINED,
        )
      }
    }
  }

  private fun inflateSingleSelectButton(
    label: String,
    value: String,
    type: SingleSelectChoiceType,
  ) {
    val singleSelectButton =
      layoutInflater.inflate(
        R.layout.chat_single_select_button,
        binding.singleSelectContainer,
        false,
      ) as TextView
    singleSelectButton.text = label
    singleSelectButton.setHapticClickListener {
      singleSelectButton.isSelected = true
      singleSelectButton.setTextColor(context.compatColor(R.color.white))
      disableSingleButtons()
      when (type) {
        SingleSelectChoiceType.UNDEFINED, // TODO: Let's talk about this one
        SingleSelectChoiceType.SELECTION,
        -> sendSingleSelect(value)
        SingleSelectChoiceType.LINK -> singleSelectLink(value)
      }
    }

    binding.singleSelectContainer.addView(singleSelectButton)
    binding.singleSelectContainer.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
  }

  private fun disableSingleButtons() {
    binding.singleSelectContainer.children.forEach { it.isEnabled = false }
  }

  private fun bindParagraphInput() {
    binding.paragraphView.avdStart()
  }

  fun rotateFileUploadIcon(isOpening: Boolean) {
    SpringAnimation(
      binding.uploadFile,
      DynamicAnimation.ROTATION,
    ).animateToFinalPosition(if (isOpening) 135f else 0f)
  }

  private fun performTextMessageSend() {
    if (binding.inputText.currentMessage.isBlank()) {
      return
    }
    sendTextMessage(binding.inputText.currentMessage)
    dismissKeyboard()
  }
}
