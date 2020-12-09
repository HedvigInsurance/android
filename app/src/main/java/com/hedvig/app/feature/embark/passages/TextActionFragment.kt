package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkTextActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.android.parcel.Parcelize
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.util.regex.Pattern

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

        binding.apply {
            messages.adapter = MessageAdapter().apply {
                submitList(data.messages)
            }

            filledTextField.hint = data.hint
            input.onChange { text ->
                if (data.mask == null) {
                    textActionSubmit.isEnabled = text.isNotEmpty()
                } else {
                    textActionSubmit.isEnabled =
                        text.isNotEmpty() && validationCheck(
                            when (data.mask) {
                                PERSONAL_NUMBER -> PERSONAL_NUMBER_REGEX
                                SWEDISH_POSTAL_CODE -> SWEDISH_POSTAL_CODE_REGEX
                                EMAIL -> EMAIL_REGEX
                                BIRTH_DATE -> BIRTH_DATE_REGEX
                                BIRTH_DATE_REVERSE -> BIRTH_DATE_REVERSE_REGEX
                                NORWEGIAN_POSTAL_CODE -> NORWEGIAN_POSTAL_CODE_REGEX
                                else -> ""
                            }, text
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
        private const val PERSONAL_NUMBER = "PersonalNumber"
        private const val SWEDISH_POSTAL_CODE = "PostalCode"
        private const val EMAIL = "Email"
        private const val BIRTH_DATE = "BirthDate"
        private const val BIRTH_DATE_REVERSE = "BirthDateReverse"
        private const val NORWEGIAN_POSTAL_CODE = "NorwegianPostalCode"

        private const val PERSONAL_NUMBER_REGEX = "^\\d{6}\\d{4}$"
        private const val SWEDISH_POSTAL_CODE_REGEX = "^\\d{3}\\d{2}$"
        private const val EMAIL_REGEX = "^.+@.+\\..+\$"
        private const val NORWEGIAN_POSTAL_CODE_REGEX = "^\\d{4}$"
        private const val BIRTH_DATE_REGEX = "^[12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"
        private const val BIRTH_DATE_REVERSE_REGEX = "^(0[1-9]|[12]\\d|3[01])-(0[1-9]|1[0-2])-[12]\\d{3}$"

        private const val DATA = "DATA"
        fun newInstance(data: TextActionData) = TextActionFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DATA, data)
            }
        }

        private fun validationCheck(regex: String, text: String) =
            Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text).find()
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
