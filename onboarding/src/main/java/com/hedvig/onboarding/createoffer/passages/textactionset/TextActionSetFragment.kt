package com.hedvig.onboarding.createoffer.passages.textactionset

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentTextActionSetBinding
import com.hedvig.app.util.extensions.hideKeyboardIfVisible
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
import com.hedvig.onboarding.createoffer.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_MILLIS
import com.hedvig.onboarding.createoffer.EmbarkViewModel
import com.hedvig.onboarding.createoffer.passages.MessageAdapter
import com.hedvig.onboarding.createoffer.passages.animateResponse
import com.hedvig.onboarding.createoffer.passages.textaction.TextFieldData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.apply {
            whenApiVersion(Build.VERSION_CODES.R) {
                inputRecycler.setupInsetsForIme(
                    root = root,
                    textActionSubmit,
                    inputLayout
                )
            }

            messages.adapter = MessageAdapter(data.messages)

            val adapter = TextInputSetAdapter(textActionSetViewModel) {
                viewLifecycleScope.launch {
                    saveAndAnimate(data)
                    model.navigateToPassage(data.link)
                }
            }
            adapter.submitList(textFieldData(data))
            inputRecycler.adapter = adapter
            textActionSubmit.text = data.submitLabel
            textActionSetViewModel.isValid.observe(viewLifecycleOwner) { textActionSubmit.isEnabled = it }

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

    private suspend fun saveAndAnimate(data: TextActionSetParameter) {
        whenApiVersion(Build.VERSION_CODES.R) {
            context?.hideKeyboardIfVisible(
                view = binding.root,
                inputView = binding.inputRecycler,
                delayMillis = 450
            )
        }
        textActionSetViewModel.inputs.value?.let { inputs ->
            data.keys.zip(inputs.values).forEach { (key, input) -> key?.let { model.putInStore(it, input) } }
            val allInput = inputs.values.joinToString(" ")
            model.putInStore("${data.passageName}Result", allInput)
            val responseText = model.preProcessResponse(data.passageName) ?: allInput
            animateResponse(binding.response, responseText)
        }
        delay(PASSAGE_ANIMATION_DELAY_MILLIS)
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
