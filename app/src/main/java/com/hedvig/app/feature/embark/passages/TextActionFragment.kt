package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import com.hedvig.app.feature.embark.setInputType
import com.hedvig.app.feature.embark.setValidationFormatter
import com.hedvig.app.feature.embark.validationCheck
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel

class TextActionFragment : Fragment(R.layout.fragment_embark_text_action) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentEmbarkTextActionBinding::bind)

    private var job: Job? = null

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
            textActionSubmit.setHapticClickListener {
                val inputText = input.text.toString()
                model.putInStore("${data.passageName}Result", inputText)
                model.putInStore(data.key, inputText)
                val responseText = model.preProcessResponse(data.passageName) ?: inputText
                job?.cancel()
                job = lifecycleScope.launch {
                    animateResponse(response, responseText)
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
