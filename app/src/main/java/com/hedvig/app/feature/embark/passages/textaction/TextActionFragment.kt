package com.hedvig.app.feature.embark.passages.textaction

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkInputItemBinding
import com.hedvig.app.databinding.FragmentTextActionSetBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.masking.derivedValues
import com.hedvig.app.feature.embark.masking.remask
import com.hedvig.app.feature.embark.masking.unmask
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.setInputType
import com.hedvig.app.feature.embark.setValidationFormatter
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.KEY_BOARD_DELAY_MILLIS
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_MILLIS
import com.hedvig.app.feature.embark.validationCheck
import com.hedvig.app.util.extensions.addViews
import com.hedvig.app.util.extensions.hideKeyboardWithDelay
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.onImeAction
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.time.Clock

/**
 * Used for Embark actions TextAction and TextActionSet
 */
class TextActionFragment : Fragment(R.layout.fragment_text_action_set) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val data: TextActionParameter
        get() = requireArguments().getParcelable(DATA)
            ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")
    private val textActionSetViewModel: TextActionViewModel by viewModel { parametersOf(data) }
    private val binding by viewBinding(FragmentTextActionSetBinding::bind)
    private val clock: Clock by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.apply {
            whenApiVersion(Build.VERSION_CODES.R) {
                inputContainer.setupInsetsForIme(
                    root = root,
                    textActionSubmit,
                    inputLayout
                )
            }
            val views = createInputViews()
            inputContainer.addViews(views)

            messages.adapter = MessageAdapter(data.messages)

            textActionSubmit.text = data.submitLabel
            textActionSetViewModel.isValid.observe(viewLifecycleOwner) { textActionSubmit.isEnabled = it }

            textActionSubmit
                .hapticClicks()
                .mapLatest { saveAndAnimate(data) }
                .onEach { model.navigateToPassage(data.link) }
                .launchIn(viewLifecycleScope)

            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }
        }
    }

    private suspend fun saveAndAnimate(data: TextActionParameter) {
        context?.hideKeyboardWithDelay(
            inputView = binding.inputContainer,
            delayMillis = KEY_BOARD_DELAY_MILLIS
        )

        textActionSetViewModel.inputs.value?.let { inputs ->
            data.keys.zip(inputs.values).forEachIndexed { index, (key, input) ->
                key?.let {
                    val mask = data.mask[index]
                    val unmasked = unmask(input, mask)
                    model.putInStore(key, unmasked)
                    derivedValues(unmasked, key, mask, clock).forEach { (key, value) ->
                        model.putInStore(key, value)
                    }
                }
            }
            val allInput = inputs.values.joinToString(" ")
            model.putInStore("${data.passageName}Result", allInput)
            val responseText = model.preProcessResponse(data.passageName) ?: allInput
            animateResponse(binding.response, responseText)
        }
        delay(PASSAGE_ANIMATION_DELAY_MILLIS)
    }

    private fun createInputViews(): List<View> = data.keys.mapIndexed { index, key ->
        val inputView = EmbarkInputItemBinding.inflate(layoutInflater, binding.inputContainer, false)

        inputView.textField.hint = data.placeholders[index]
        val mask = data.mask[index]
        mask?.let {
            inputView.input.apply {
                setInputType(it)
                setValidationFormatter(it)
            }
        }
        inputView.input.onChange { text ->
            if (mask == null) {
                if (text.isBlank()) {
                    textActionSetViewModel.updateIsValid(index, false)
                } else {
                    textActionSetViewModel.updateIsValid(index, true)
                }
            } else {
                if (text.isNotBlank() && validationCheck(mask, text)) {
                    textActionSetViewModel.updateIsValid(index, true)
                } else {
                    textActionSetViewModel.updateIsValid(index, false)
                }
            }
            textActionSetViewModel.setInputValue(index, text)
        }

        if (index < data.keys.size - 1) {
            inputView.input.imeOptions = EditorInfo.IME_ACTION_NEXT
        } else {
            inputView.input.imeOptions = EditorInfo.IME_ACTION_DONE
        }

        inputView.input.onImeAction {
            if (textActionSetViewModel.isValid.value == true) {
                viewLifecycleScope.launch {
                    saveAndAnimate(data)
                    model.navigateToPassage(data.link)
                }
            }
        }

        key?.let(model::getFromStore)
            ?.let { remask(it, mask) }
            ?.let(inputView.input::setText)
        inputView.root
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: TextActionParameter) = TextActionFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DATA, data)
            }
        }
    }
}
