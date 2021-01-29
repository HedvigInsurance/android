package com.hedvig.app.feature.embark.passages.textaction

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkTextActionBinding
import com.hedvig.app.feature.embark.BIRTH_DATE
import com.hedvig.app.feature.embark.BIRTH_DATE_REVERSE
import com.hedvig.app.feature.embark.EMAIL
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.NORWEGIAN_POSTAL_CODE
import com.hedvig.app.feature.embark.PERSONAL_NUMBER
import com.hedvig.app.feature.embark.SWEDISH_POSTAL_CODE
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.UpgradeAppFragment
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.setInputType
import com.hedvig.app.feature.embark.setValidationFormatter
import com.hedvig.app.feature.embark.validationCheck
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.android.viewmodel.ext.android.sharedViewModel

class TextActionFragment : Fragment(R.layout.fragment_embark_text_action) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentEmbarkTextActionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireArguments().getParcelable<TextActionParameter>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        if (data.mask != null) {
            if (!(data.mask == PERSONAL_NUMBER ||
                    data.mask == SWEDISH_POSTAL_CODE ||
                    data.mask == EMAIL ||
                    data.mask == BIRTH_DATE ||
                    data.mask == BIRTH_DATE_REVERSE ||
                    data.mask == NORWEGIAN_POSTAL_CODE)
            ) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.passageContainer, UpgradeAppFragment.newInstance())
                    .commit()
            }
        }

        binding.apply {
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

            textActionSubmit.text = data.submitLabel
            textActionSubmit
                .hapticClicks()
                .mapLatest {
                    textActionSubmit.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    val inputText = input.text.toString()
                    model.putInStore("${data.passageName}Result", inputText)
                    model.putInStore(data.key, inputText)
                    val responseText = model.preProcessResponse(data.passageName) ?: inputText
                    animateResponse(response, responseText)
                }
                .onEach { model.navigateToPassage(data.link) }
                .launchIn(lifecycleScope)
        }
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
