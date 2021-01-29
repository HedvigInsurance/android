package com.hedvig.app.feature.embark.passages.textactionset

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentTextActionSetBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.passages.textaction.TextFieldData
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class TextActionSetFragment : Fragment(R.layout.fragment_text_action_set) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val textActionSetViewModel: TextActionSetViewModel by viewModel()
    private val binding by viewBinding(FragmentTextActionSetBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val data = requireArguments().getParcelable<TextActionSetParameter>(DATA)
        if (data == null) {
            e { "Programmer error: Data is null in ${this.javaClass.name}" }
            return
        }

        binding.apply {
            messages.adapter = MessageAdapter(data.messages)
            inputRecycler.adapter = TextInputSetAdapter(textActionSetViewModel).also {
                it.submitList(textFieldData(data))
            }
            textActionSubmit.text = data.submitLabel
            textActionSetViewModel.isValid.observe(viewLifecycleOwner) { hashMap ->
                if (hashMap.isNotEmpty()) {
                    textActionSubmit.isEnabled = hashMap.all { it.value }
                }
            }
            textActionSubmit
                .hapticClicks()
                .mapLatest { animateSubmission(data) }
                .onEach { model.navigateToPassage(data.link) }
                .launchIn(lifecycleScope)
        }
    }

    private suspend fun FragmentTextActionSetBinding.animateSubmission(data: TextActionSetParameter) {
        val allInput = ""
        for ((index, key) in data.keys.withIndex()) {
            val input =
                inputRecycler.getChildAt(index).findViewById<TextInputEditText>(R.id.input)
            key?.let { model.putInStore(it, input.text.toString()) }
            allInput.plus("${input.text.toString()} ")
        }
        val responseText = model.preProcessResponse(data.passageName) ?: allInput
        animateResponse(response, responseText)
    }

    private fun textFieldData(data: TextActionSetParameter): MutableList<TextFieldData> {
        val list = mutableListOf<TextFieldData>()
        for ((index, key) in data.keys.withIndex()) {
            list.add(TextFieldData(key, data.placeholders[index], data.mask[index]))
        }
        return list
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: TextActionSetParameter) = TextActionSetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DATA, data)
            }
        }
    }
}
