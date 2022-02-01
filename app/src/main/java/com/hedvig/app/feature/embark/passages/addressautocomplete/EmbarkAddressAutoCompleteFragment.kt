package com.hedvig.app.feature.embark.passages.addressautocomplete

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkAddressAutoCompleteActionBinding
import com.hedvig.app.feature.addressautocompletion.activityresult.FetchDanishAddressAutoCompleteContractHandler
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.addressautocomplete.composables.AddressCard
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.util.extensions.hideKeyboardWithDelay
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EmbarkAddressAutoCompleteFragment : Fragment(R.layout.fragment_embark_address_auto_complete_action) {

    private val data: EmbarkAddressAutoCompleteParams
        get() = requireArguments().getParcelable(DATA)
            ?: throw Error("Programmer error: DATA is null in ${this.javaClass.name}")

    private val embarkViewModel: EmbarkViewModel by sharedViewModel()
    private val viewModel: EmbarkAddressAutoCompleteViewModel by viewModel {
        val messages = data.messages
        val placeholderText = data.placeholder
        val prefilledAddress = embarkViewModel.getPrefillFromStore(data.key)
        parametersOf(messages, prefilledAddress ?: placeholderText)
    }
    private val binding by viewBinding(FragmentEmbarkAddressAutoCompleteActionBinding::bind)

    private lateinit var fetchDanishAddressAutoCompleteContractHandler: FetchDanishAddressAutoCompleteContractHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchDanishAddressAutoCompleteContractHandler = FetchDanishAddressAutoCompleteContractHandler(
            registry = requireActivity().activityResultRegistry,
            onAddressResult = { newAddress ->
                viewModel.updateAddressSelected(newAddress)
            }
        )
        lifecycle.addObserver(fetchDanishAddressAutoCompleteContractHandler)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        val messageAdapter = MessageAdapter(emptyList())
        binding.textActionSubmit.text = "Submit"
        binding.textActionSubmit
            .hapticClicks()
            .mapLatest { saveAndAnimate(data) }
            .onEach { embarkViewModel.submitAction(data.link) }
            .launchIn(viewLifecycleScope)
        viewModel.viewState
            .flowWithLifecycle(viewLifecycle)
            .onEach { viewState ->
                messageAdapter.submitList(viewState.messages)
                binding.inputCard.setContent {
                    AddressCard(
                        addressText = viewState.address,
                        onClick = {
                            fetchDanishAddressAutoCompleteContractHandler.startAutoCompletionActivity(
                                viewState.address
                            )
                        }
                    )
                }
            }
            .launchIn(viewLifecycleScope)

        // We need to wait for all input views to be laid out before starting enter transition.
        // This could perhaps be handled with a callback from the inputContainer.
        viewLifecycleScope.launchWhenCreated {
            delay(50)
            startPostponedEnterTransition()
        }
    }

    private suspend fun saveAndAnimate(data: EmbarkAddressAutoCompleteParams) {
        context?.hideKeyboardWithDelay(
            inputView = binding.inputCard,
            delayMillis = EmbarkActivity.KEY_BOARD_DELAY_MILLIS
        )
        embarkViewModel.putInStore(data.key, viewModel.viewState.value.address)
    }

    companion object {
        private const val DATA = "DATA"

        fun newInstance(params: EmbarkAddressAutoCompleteParams) = EmbarkAddressAutoCompleteFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DATA, params)
            }
        }
    }
}
