package com.hedvig.app.feature.embark.passages.textaction

import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkTextActionBinding
import com.hedvig.app.feature.embark.BIRTH_DATE
import com.hedvig.app.feature.embark.BIRTH_DATE_REVERSE
import com.hedvig.app.feature.embark.EMAIL
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.NORWEGIAN_POSTAL_CODE
import com.hedvig.app.feature.embark.PERSONAL_NUMBER
import com.hedvig.app.feature.embark.SWEDISH_POSTAL_CODE
import com.hedvig.app.feature.embark.masking.derivedValues
import com.hedvig.app.feature.embark.masking.remask
import com.hedvig.app.feature.embark.masking.unmask
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.UpgradeAppFragment
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.setInputType
import com.hedvig.app.feature.embark.setValidationFormatter
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.KEY_BOARD_DELAY_MILLIS
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_MILLIS
import com.hedvig.app.feature.embark.validationCheck
import com.hedvig.app.util.extensions.hideKeyboard
import com.hedvig.app.util.extensions.hideKeyboardWithDelay
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.onImeAction
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
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
                    input
                )
            }

            messages.adapter = MessageAdapter(data.messages)

            input.hint = data.hint
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
        context?.hideKeyboardWithDelay(
            inputView = binding.input,
            delayMillis = KEY_BOARD_DELAY_MILLIS
        )
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
