package com.hedvig.onboarding.createoffer.passages.textaction

import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.util.extensions.hideKeyboardIfVisible
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.onImeAction
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
import com.hedvig.onboarding.databinding.FragmentEmbarkTextActionBinding
import com.hedvig.onboarding.createoffer.BIRTH_DATE
import com.hedvig.onboarding.createoffer.BIRTH_DATE_REVERSE
import com.hedvig.onboarding.createoffer.EMAIL
import com.hedvig.onboarding.createoffer.EmbarkViewModel
import com.hedvig.onboarding.createoffer.NORWEGIAN_POSTAL_CODE
import com.hedvig.onboarding.createoffer.PERSONAL_NUMBER
import com.hedvig.onboarding.createoffer.SWEDISH_POSTAL_CODE
import com.hedvig.onboarding.createoffer.masking.derivedValues
import com.hedvig.onboarding.createoffer.masking.remask
import com.hedvig.onboarding.createoffer.masking.unmask
import com.hedvig.onboarding.createoffer.passages.MessageAdapter
import com.hedvig.onboarding.createoffer.passages.UpgradeAppFragment
import com.hedvig.onboarding.createoffer.passages.animateResponse
import com.hedvig.onboarding.createoffer.setInputType
import com.hedvig.onboarding.createoffer.setValidationFormatter
import com.hedvig.onboarding.createoffer.validationCheck
import e
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.time.Clock

class TextActionFragment : Fragment(R.layout.fragment_embark_text_action) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentEmbarkTextActionBinding::bind)

    private val clock: Clock by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        val data = requireArguments().getParcelable<TextActionParameter>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        when (data.mask) {
            PERSONAL_NUMBER,
            SWEDISH_POSTAL_CODE,
            EMAIL,
            BIRTH_DATE,
            BIRTH_DATE_REVERSE,
            NORWEGIAN_POSTAL_CODE,
            null,
            -> {
            }
            else -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.passageContainer, UpgradeAppFragment.newInstance())
                    .commit()
                return
            }
        }

        binding.apply {

            whenApiVersion(Build.VERSION_CODES.R) {
                input.setupInsetsForIme(
                    root = root,
                    textActionSubmit,
                    inputLayout
                )
            }

            messages.adapter = MessageAdapter(data.messages)

            filledTextField.hint = data.hint
            data.mask?.let { mask ->
                input.apply {
                    setInputType(mask)
                    setValidationFormatter(mask)
                }
            }
    
            input.onChange { text ->
                if (data.mask == null) {
                    textActionSubmit.isEnabled = text.isNotEmpty()
                } else {
                    textActionSubmit.isEnabled =
                        text.isNotEmpty() && validationCheck(
                            data.mask, text
                        )
                }
            }

            input.onImeAction {
                if (textActionSubmit.isEnabled) {
                    viewLifecycleScope.launch {
                        saveAndAnimate(data)
                        model.navigateToPassage(data.link)
                    }
                }
            }

            model.getFromStore(data.key)?.let { input.setText(remask(it, data.mask)) }

            textActionSubmit.text = data.submitLabel
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
        whenApiVersion(Build.VERSION_CODES.R) {
            context?.hideKeyboardIfVisible(
                view = binding.root,
                inputView = binding.input,
                delayMillis = 450
            )
        }
        binding.textActionSubmit.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val inputText = binding.input.text.toString()
        val unmasked = unmask(inputText, data.mask)
        model.putInStore("${data.passageName}Result", unmasked)
        model.putInStore(data.key, unmasked)
        derivedValues(unmasked, data.key, data.mask, clock).forEach { (key, value) ->
            model.putInStore(key, value)
        }
        val responseText = model.preProcessResponse(data.passageName) ?: inputText
        animateResponse(binding.response, responseText)
        delay(PASSAGE_ANIMATION_DELAY_MILLIS)
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
