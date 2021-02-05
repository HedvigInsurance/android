package com.hedvig.app.feature.embark.passages.textactionset

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentTextActionSetBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.passages.textaction.TextFieldData
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewLifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TextActionSetFragment : Fragment(R.layout.fragment_text_action_set) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val data: TextActionSetParameter
        get() = requireArguments().getParcelable(DATA)
            ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")
    private val textActionSetViewModel: TextActionSetViewModel by viewModel { parametersOf(data) }
    private val binding by viewBinding(FragmentTextActionSetBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            messages.adapter = MessageAdapter(data.messages)
            inputRecycler.adapter = TextInputSetAdapter(textActionSetViewModel).also {
                it.submitList(textFieldData(data))
            }
            textActionSubmit.text = data.submitLabel
            textActionSetViewModel.isValid.observe(viewLifecycleOwner) { textActionSubmit.isEnabled = it }

            textActionSubmit
                .hapticClicks()
                .mapLatest { animateSubmission(data) }
                .onEach {
                    model.navigateToPassage(data.link)
                }
                .launchIn(viewLifecycleScope)
        }
    }

    private suspend fun FragmentTextActionSetBinding.animateSubmission(data: TextActionSetParameter) {
        textActionSetViewModel.inputs.value?.let { inputs ->
            data.keys.zip(inputs.values).forEach { (key, input) -> key?.let { model.putInStore(it, input) } }
            val allInput = inputs.values.joinToString(" ")
            model.putInStore("${data.passageName}Result", allInput)
            val responseText = model.preProcessResponse(data.passageName) ?: allInput
            animateResponse(response, responseText)
        }
    }

    private fun textFieldData(data: TextActionSetParameter) =
        data.keys.mapIndexed { index, key ->
            TextFieldData(
                key,
                data.placeholders[index],
                data.mask[index],
                key?.let { model.getFromStore(it) },
            )
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
