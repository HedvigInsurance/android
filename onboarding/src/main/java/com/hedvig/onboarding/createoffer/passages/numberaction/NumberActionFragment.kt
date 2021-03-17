package com.hedvig.onboarding.createoffer.passages.numberaction

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.hedvig.onboarding.R
import com.hedvig.app.util.extensions.hideKeyboardIfVisible
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.onImeAction
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
import com.hedvig.onboarding.createoffer.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_MILLIS
import com.hedvig.onboarding.createoffer.EmbarkViewModel
import com.hedvig.onboarding.createoffer.passages.MessageAdapter
import com.hedvig.onboarding.createoffer.passages.animateResponse
import com.hedvig.onboarding.databinding.NumberActionFragmentBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NumberActionFragment : Fragment(R.layout.number_action_fragment) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(NumberActionFragmentBinding::bind)
    private val data: NumberActionParams
        get() = requireArguments().getParcelable(PARAMS)
            ?: throw Error("Programmer error: No PARAMS provided to ${this.javaClass.name}")
    private val numberActionViewModel: NumberActionViewModel by viewModel { parametersOf(data) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()

        with(binding) {
            whenApiVersion(Build.VERSION_CODES.R) {
                input.setupInsetsForIme(
                    root = root,
                    inputLayout,
                    submit,
                )
            }

            messages.adapter = MessageAdapter(data.messages)
            inputContainer.placeholderText = data.placeholder
            data.label?.let { inputContainer.hint = it }
            data.unit?.let { inputContainer.helperText = it }
            input.doOnTextChanged { text, _, _, _ ->
                numberActionViewModel.validate(text)
            }
            input.onImeAction {
                if (numberActionViewModel.valid.value == true) {
                    viewLifecycleScope.launch {
                        saveAndAnimate()
                        model.navigateToPassage(data.link)
                    }
                }
            }
            numberActionViewModel.valid.observe(viewLifecycleOwner) { submit.isEnabled = it }
            model.getFromStore(data.key)?.let { input.setText(it) }
            submit.text = data.submitLabel
            submit
                .hapticClicks()
                .mapLatest { saveAndAnimate() }
                .onEach { model.navigateToPassage(data.link) }
                .launchIn(viewLifecycleScope)

            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }
        }
    }

    private suspend fun saveAndAnimate() {
        whenApiVersion(Build.VERSION_CODES.R) {
            context?.hideKeyboardIfVisible(
                view = binding.root,
                inputView = binding.input,
                delayMillis = 450
            )
        }

        val inputText = binding.input.text.toString()
        model.putInStore("${data.passageName}Result", inputText)
        model.putInStore(data.key, inputText)
        val responseText = model.preProcessResponse(data.passageName) ?: inputText
        animateResponse(binding.response, responseText)
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
