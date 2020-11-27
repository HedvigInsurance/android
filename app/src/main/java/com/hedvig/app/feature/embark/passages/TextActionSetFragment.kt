package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentTextActionSetBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.android.parcel.Parcelize
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class TextActionSetFragment : Fragment(R.layout.fragment_text_action_set) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val textActionSetViewModel: TextActionSetViewModel by viewModel()
    private val binding by viewBinding(FragmentTextActionSetBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val data = requireArguments().getParcelable<TextActionSetData>(DATA)
        if (data == null) {
            e { "Programmer error: Data is null in ${this.javaClass.name}" }
            return
        }

        binding.apply {
            messages.adapter = MessageAdapter().apply {
                submitList(data.messages)
            }

            inputRecycler.adapter = TextInputSetAdapter(textActionSetViewModel).also {
                it.submitList(textFieldData(data))
            }
            textActionSubmit.text = data.submitLabel
            textActionSetViewModel.hasText.observe(viewLifecycleOwner) { hashMap ->
                if (hashMap.isNotEmpty()) {
                    textActionSubmit.isEnabled = hashMap.all { it.value }
                }
            }
            textActionSubmit.setHapticClickListener {
                val allInput = ""
                for ((index, key) in data.keys.withIndex()) {
                    val input =
                        inputRecycler.getChildAt(index).findViewById<TextInputEditText>(R.id.input)
                    key?.let { model.putInStore(it, input.text.toString()) }
                    allInput.plus("${input.text.toString()} ")
                }
                val responseText =
                    model.preProcessResponse(data.passageName) ?: allInput
                animateResponse(response, responseText) {
                    model.navigateToPassage(data.link)
                }
            }
        }
    }

    private fun textFieldData(data: TextActionSetData): MutableList<TextFieldData> {
        val list = mutableListOf<TextFieldData>()
        for ((index, key) in data.keys.withIndex()) {
            list.add(TextFieldData(key, data.placeholders[index]))
        }
        return list
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
    val placeholders: List<String?>,
    val keys: List<String?>,
    val messages: List<String>,
    val submitLabel: String,
    val passageName: String
) : Parcelable {
    companion object {
        fun from(messages: List<String>, data: EmbarkStoryQuery.Data3, passageName: String) =
            TextActionSetData(
                link = data.link.fragments.embarkLinkFragment.name,
                placeholders = data.textActions.map { it.data?.placeholder },
                keys = data.textActions.map { it.data?.key },
                messages = messages,
                submitLabel = data.link.fragments.embarkLinkFragment.label,
                passageName = passageName
            )
    }
}
