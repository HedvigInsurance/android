package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentTextActionSetBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.android.parcel.Parcelize
import org.koin.android.viewmodel.ext.android.sharedViewModel

class TextActionSetFragment : Fragment(R.layout.fragment_text_action_set) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentTextActionSetBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val data = requireArguments().getParcelable<TextActionSetData>(DATA)
        if (data?.firstKey == null || data.secondKey == null || data.firstPlaceholder == null || data.secondPlaceholder == null) {
            e { "Programmer error: Some or all data is null in ${this.javaClass.name}" }
            return
        }

        binding.apply {
            messages.adapter = MessageAdapter().apply {
                submitList(data.messages)
            }

            firstTextField.hint = data.firstPlaceholder
            secondTextField.hint = data.secondPlaceholder
            var isFirstEmpty = true
            var isSecondEmpty = true
            var canProceed: Boolean
            firstInput.onChange { text ->
                if (text.isNotBlank()) {
                    isFirstEmpty = false
                    canProceed = !isSecondEmpty
                } else {
                    isFirstEmpty = true
                    canProceed = false
                }
                textActionSubmit.isEnabled = canProceed
            }
            secondInput.onChange { text ->
                if (text.isNotBlank()) {
                    isSecondEmpty = false
                    canProceed = !isFirstEmpty
                } else {
                    isSecondEmpty = true
                    canProceed = false
                }
                textActionSubmit.isEnabled = canProceed
            }
            textActionSubmit.text = data.submitLabel
            textActionSubmit.setHapticClickListener {
                val firstInput = firstInput.text.toString()
                val secondInput = secondInput.text.toString()
                model.putInStore("${data.passageName}Result", "$firstInput $secondInput")
                model.putInStore(data.firstKey, firstInput)
                model.putInStore(data.secondKey, secondInput)
                val responseText =
                    model.preProcessResponse(data.passageName) ?: "$firstInput $secondInput"
                animateResponse(response, responseText) {
                    model.navigateToPassage(data.link)
                }
            }
        }
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: TextActionSetData) = TextActionSetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DATA, data)
            }
        }
    }
}

@Parcelize
data class TextActionSetData(
    val link: String,
    val firstPlaceholder: String?,
    val secondPlaceholder: String?,
    val firstKey: String?,
    val secondKey: String?,
    val messages: List<String>,
    val submitLabel: String,
    val passageName: String
) : Parcelable {
    companion object {
        fun from(messages: List<String>, data: EmbarkStoryQuery.Data3, passageName: String) =
            TextActionSetData(
                link = data.link.fragments.embarkLinkFragment.name,
                firstPlaceholder = data.textActions[0].data?.placeholder,
                secondPlaceholder = data.textActions[1].data?.placeholder,
                firstKey = data.textActions[0].data?.key,
                secondKey = data.textActions[1].data?.key,
                messages = messages,
                submitLabel = data.link.fragments.embarkLinkFragment.label,
                passageName = passageName
            )
    }
}
