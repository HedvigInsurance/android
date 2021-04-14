package com.hedvig.app.feature.embark.passages.numberactionset

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkInputItemBinding
import com.hedvig.app.databinding.NumberActionSetFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.KEY_BOARD_DELAY_MILLIS
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_MILLIS
import com.hedvig.app.util.extensions.addViews
import com.hedvig.app.util.extensions.hideKeyboardWithDelay
import com.hedvig.app.util.extensions.onImeAction
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Used for Embark actions NumberAction and NumberActionSet
 */
class NumberActionFragment : Fragment(R.layout.number_action_set_fragment) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(NumberActionSetFragmentBinding::bind)
    private val data: NumberActionParams
        get() = requireArguments().getParcelable(PARAMS)
            ?: throw Error("Programmer error: No PARAMS provided to ${this.javaClass.name}")
    private val numberActionViewModel: NumberActionViewModel by viewModel { parametersOf(data) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()

        with(binding) {
            whenApiVersion(Build.VERSION_CODES.R) {
                inputContainer.setupInsetsForIme(
                    root = root,
                    submit,
                    inputLayout,
                )
            }

            messages.adapter = MessageAdapter(data.messages)
            val views = createInputViews()
            inputContainer.addViews(views)

            numberActionViewModel.valid.observe(viewLifecycleOwner) { submit.isEnabled = it }
            submit.text = data.submitLabel
            submit
                .hapticClicks()
                .mapLatest { saveAndAnimate() }
                .onEach { model.navigateToPassage(data.link) }
                .launchIn(viewLifecycleScope)

            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }

            numberActionViewModel.valid.observe(viewLifecycleOwner) {
                submit.isEnabled = it
            }
        }
    }

    private fun createInputViews(): List<View> {
        return data.numberActions.mapIndexed { index, numberAction ->
            val binding = EmbarkInputItemBinding.inflate(layoutInflater, binding.inputContainer, false)

            numberAction.title.let { binding.textField.hint = it }
            numberAction.unit?.let { binding.textField.helperText = it }

            binding.input.hint = numberAction.placeholder
            binding.input.doOnTextChanged { text, _, _, _ ->
                numberActionViewModel.setInputValue(numberAction.key, text.toString())
            }

            binding.input.inputType = InputType.TYPE_CLASS_NUMBER
            val imeOptions = if (index < data.numberActions.size - 1) {
                EditorInfo.IME_ACTION_NEXT
            } else {
                EditorInfo.IME_ACTION_DONE
            }
            binding.input.imeOptions = imeOptions
            binding.input.onImeAction(imeActionId = imeOptions) {
                if (numberActionViewModel.valid.value == true) {
                    viewLifecycleScope.launch {
                        saveAndAnimate()
                        model.navigateToPassage(data.link)
                    }
                }
            }

            model.getFromStore(numberAction.key)?.let { binding.input.setText(it) }
            binding.root
        }
    }

    private suspend fun saveAndAnimate() {
        context?.hideKeyboardWithDelay(
            inputView = binding.inputLayout,
            delayMillis = KEY_BOARD_DELAY_MILLIS
        )
        numberActionViewModel.onContinue(model::putInStore)
        val responseText = model.preProcessResponse(data.passageName)
        val allInput = numberActionViewModel.getAllInput()
        animateResponse(binding.response, responseText ?: allInput ?: "")
        delay(PASSAGE_ANIMATION_DELAY_MILLIS)
    }

    companion object {
        private const val PARAMS = "PARAMS"
        fun newInstance(params: NumberActionParams) = NumberActionFragment().apply {
            arguments = bundleOf(
                PARAMS to params
            )
        }
    }
}
