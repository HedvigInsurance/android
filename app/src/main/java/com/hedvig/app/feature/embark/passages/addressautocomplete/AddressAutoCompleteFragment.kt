package com.hedvig.app.feature.embark.passages.addressautocomplete

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkAddressAutoCompleteActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.util.extensions.hideKeyboardWithDelay
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.onImeAction
import com.hedvig.app.util.extensions.showKeyboardWithDelay
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AddressAutoCompleteFragment : Fragment(R.layout.fragment_embark_address_auto_complete_action) {

    private val data: AddressAutoCompleteParams
        get() = requireArguments().getParcelable(DATA)
            ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")

    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentEmbarkAddressAutoCompleteActionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.apply {
            whenApiVersion(Build.VERSION_CODES.R) {
                input.setupInsetsForIme(
                    root = root,
                    textActionSubmit,
                    inputLayout
                )
            }
            setupInputView()
            viewLifecycleScope.launchWhenCreated {
                requireContext().showKeyboardWithDelay(input, 500)
            }

            messages.adapter = MessageAdapter(data.messages)

            textActionSubmit.text = "Submit"
            textActionSubmit
                .hapticClicks()
                .mapLatest { saveAndAnimate(data) }
                .onEach { model.submitAction(data.link) }
                .launchIn(viewLifecycleScope)

            // We need to wait for all input views to be laid out before starting enter transition.
            // This could perhaps be handled with a callback from the inputContainer.
            viewLifecycleScope.launchWhenCreated {
                delay(50)
                startPostponedEnterTransition()
            }
        }
    }

    private fun setupInputView() {
        binding.filledTextField.isExpandedHintEnabled = false
        binding.filledTextField.placeholderText = data.placeholder
        binding.input.imeOptions = EditorInfo.IME_ACTION_DONE

        binding.input.onChange { text ->
        }

        binding.input.onImeAction(imeActionId = EditorInfo.IME_ACTION_DONE) {
            viewLifecycleScope.launch {
                saveAndAnimate(data)
                model.submitAction(data.link)
            }
        }

        data.key.let(model::getPrefillFromStore)
            ?.let(binding.input::setText)
    }

    private suspend fun saveAndAnimate(data: AddressAutoCompleteParams) {
        context?.hideKeyboardWithDelay(
            inputView = binding.inputLayout,
            delayMillis = EmbarkActivity.KEY_BOARD_DELAY_MILLIS
        )
    }

    companion object {
        private const val DATA = "DATA"

        fun newInstance(params: AddressAutoCompleteParams) = AddressAutoCompleteFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DATA, params)
            }
        }
    }
}
