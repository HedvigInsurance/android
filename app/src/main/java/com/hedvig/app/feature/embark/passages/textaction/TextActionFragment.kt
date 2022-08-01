package com.hedvig.app.feature.embark.passages.textaction

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.hedvig.android.core.common.android.whenApiVersion
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkInputItemBinding
import com.hedvig.app.databinding.FragmentTextActionSetBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.Response
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.KEYBOARD_HIDE_DELAY_DURATION
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_DURATION
import com.hedvig.app.feature.embark.util.setInputType
import com.hedvig.app.feature.embark.util.setValidationFormatter
import com.hedvig.app.util.extensions.addViews
import com.hedvig.app.util.extensions.hideKeyboardWithDelay
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.onImeAction
import com.hedvig.app.util.extensions.showKeyboardWithDelay
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

/**
 * Used for Embark actions TextAction and TextActionSet
 */
class TextActionFragment : Fragment(R.layout.fragment_text_action_set) {
  private val data: TextActionParameter
    get() = requireArguments().getParcelable(DATA)
      ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")
  private val textActionViewModel: TextActionViewModel by viewModel { parametersOf(data) }
  private val embarkViewModel: EmbarkViewModel by sharedViewModel()
  private val binding by viewBinding(FragmentTextActionSetBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    postponeEnterTransition()

    binding.apply {
      whenApiVersion(Build.VERSION_CODES.R) {
        inputContainer.setupInsetsForIme(
          root = root,
          textActionSubmit,
          inputLayout,
        )
      }
      val views = createInputViews()
      views.firstOrNull()?.let {
        val input = it.findViewById<TextInputEditText>(R.id.input)
        viewLifecycleScope.launchWhenCreated {
          requireContext().showKeyboardWithDelay(input, 500.milliseconds)
        }
      }

      inputContainer.addViews(views)

      messages.adapter = MessageAdapter(data.messages)

      textActionSubmit.text = data.submitLabel
      textActionViewModel.isValid.observe(viewLifecycleOwner) { textActionSubmit.isEnabled = it }

      textActionSubmit
        .hapticClicks()
        .mapLatest { saveAndAnimate(data) }
        .onEach { embarkViewModel.submitAction(data.link) }
        .launchIn(viewLifecycleScope)

      // We need to wait for all input views to be laid out before starting enter transition.
      // This could perhaps be handled with a callback from the inputContainer.
      viewLifecycleScope.launchWhenCreated {
        delay(50.milliseconds)
        startPostponedEnterTransition()
      }
    }
  }

  private suspend fun saveAndAnimate(data: TextActionParameter) {
    context?.hideKeyboardWithDelay(
      inputView = binding.inputContainer,
      delayDuration = KEYBOARD_HIDE_DELAY_DURATION,
    )

    textActionViewModel.inputs.value?.let { inputs ->
      data.keys.zip(inputs.values).forEachIndexed { index, (key, input) ->
        key?.let {
          val mask = data.masks.getOrNull(index)
          val unmasked = mask?.unMask(input) ?: input
          embarkViewModel.putInStore(key, unmasked)
          mask?.derivedValues(unmasked, key, LocalDate.now())?.forEach { (key, value) ->
            embarkViewModel.putInStore(key, value)
          }
        }
      }
      val allInput = inputs.values.joinToString(" ")
      embarkViewModel.putInStore("${data.passageName}Result", allInput)
      val response =
        embarkViewModel.preProcessResponse(data.passageName) ?: Response.SingleResponse(allInput)
      animateResponse(binding.responseContainer, response)
    }
    delay(PASSAGE_ANIMATION_DELAY_DURATION)
  }

  private fun createInputViews(): List<View> = data.keys.mapIndexed { index, key ->
    val inputView = EmbarkInputItemBinding.inflate(layoutInflater, binding.inputContainer, false)

    inputView.textField.isExpandedHintEnabled = false
    data.hints.getOrNull(index)?.let { inputView.textField.hint = it }
    data.subtitles.getOrNull(index)?.let { inputView.textField.hint = it }
    data.placeholders.getOrNull(index)?.let { inputView.textField.placeholderText = it }
    val mask = data.masks.getOrNull(index)
    mask?.let {
      inputView.input.apply {
        setInputType(it)
        setValidationFormatter(it)
      }
    }
    inputView.input.onChange { text ->
      if (mask == null) {
        if (text.isBlank()) {
          textActionViewModel.updateIsValid(index, false)
        } else {
          textActionViewModel.updateIsValid(index, true)
        }
      } else {
        if (text.isNotBlank() && mask.isValid(text)) {
          textActionViewModel.updateIsValid(index, true)
        } else {
          textActionViewModel.updateIsValid(index, false)
        }
      }
      textActionViewModel.setInputValue(index, text)
    }

    val imeOptions = if (index < data.keys.size - 1) {
      EditorInfo.IME_ACTION_NEXT
    } else {
      EditorInfo.IME_ACTION_DONE
    }

    inputView.input.imeOptions = imeOptions

    if (imeOptions == EditorInfo.IME_ACTION_DONE) {
      inputView.input.onImeAction(imeActionId = imeOptions) {
        if (textActionViewModel.isValid.value == true) {
          viewLifecycleScope.launch {
            saveAndAnimate(data)
            embarkViewModel.submitAction(data.link)
          }
        }
      }
    }

    key?.let(embarkViewModel::getPrefillFromStore)
      ?.let { mask?.mask(it) ?: it }
      ?.let(inputView.input::setText)
    inputView.root
  }

  companion object {
    private const val DATA = "DATA"
    fun newInstance(data: TextActionParameter) = TextActionFragment().apply {
      arguments = bundleOf(
        DATA to data,
      )
    }
  }
}
