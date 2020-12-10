package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkTextActionBinding
import com.hedvig.app.feature.embark.BIRTH_DATE
import com.hedvig.app.feature.embark.BIRTH_DATE_REVERSE
import com.hedvig.app.feature.embark.EMAIL
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.NORWEGIAN_POSTAL_CODE
import com.hedvig.app.feature.embark.PERSONAL_NUMBER
import com.hedvig.app.feature.embark.SWEDISH_POSTAL_CODE
import com.hedvig.app.feature.embark.validationCheck
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.android.parcel.Parcelize
import org.koin.android.viewmodel.ext.android.sharedViewModel

class TextActionFragment : Fragment(R.layout.fragment_embark_text_action) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentEmbarkTextActionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireArguments().getParcelable<TextActionData>(DATA)

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
            messages.adapter = MessageAdapter().apply {
                submitList(data.messages)
            }

            filledTextField.hint = data.hint

            if (data.mask == EMAIL) {
                input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
            if (data.mask == BIRTH_DATE ||
                data.mask == BIRTH_DATE_REVERSE ||
                data.mask == PERSONAL_NUMBER ||
                data.mask == SWEDISH_POSTAL_CODE ||
                data.mask == NORWEGIAN_POSTAL_CODE
            ) {
                input.keyListener = DigitsKeyListener.getInstance(
                    when (data.mask) {
                        PERSONAL_NUMBER,
                        BIRTH_DATE,
                        BIRTH_DATE_REVERSE -> "0123456789-"
                        NORWEGIAN_POSTAL_CODE -> "0123456789"
                        SWEDISH_POSTAL_CODE -> "0123456789 "
                        else -> "0123456789- "
                    }
                )
                var prevLength = 0
                input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int
                    ) {
                        prevLength = input.text.toString().length
                    }

                    override fun onTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int
                    ) {
                    }

                    override fun afterTextChanged(editable: Editable) {
                        val length = editable.length
                        when (data.mask) {
                            PERSONAL_NUMBER -> {
                                if (prevLength < length && length == 6) {
                                    editable.append("-")
                                }
                            }
                            SWEDISH_POSTAL_CODE -> {
                                if (prevLength < length && length == 3) {
                                    editable.append(" ")
                                }
                            }
                            BIRTH_DATE -> {
                                if (prevLength < length && (length == 4 || length == 7)) {
                                    editable.append("-")
                                }
                            }
                            BIRTH_DATE_REVERSE -> {
                                if (prevLength < length && (length == 2 || length == 5)) {
                                    editable.append("-")
                                }
                            }
                        }
                    }
                })
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
            textActionSubmit.setHapticClickListener {
                val inputText = input.text.toString()
                model.putInStore("${data.passageName}Result", inputText)
                model.putInStore(data.key, inputText)
                val responseText = model.preProcessResponse(data.passageName) ?: inputText
                animateResponse(response, responseText) {
                    model.navigateToPassage(data.link)
                }
            }
        }
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: TextActionData) = TextActionFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DATA, data)
            }
        }
    }
}

@Parcelize
data class TextActionData(
    val link: String,
    val hint: String,
    val messages: List<String>,
    val submitLabel: String,
    val key: String,
    val passageName: String,
    val mask: String?
) : Parcelable {
    companion object {
        fun from(messages: List<String>, data: EmbarkStoryQuery.Data2, passageName: String) =
            TextActionData(
                link = data.link.fragments.embarkLinkFragment.name,
                hint = data.placeholder,
                messages = messages,
                submitLabel = data.link.fragments.embarkLinkFragment.label,
                key = data.key,
                passageName = passageName,
                mask = data.mask
            )
    }
}
