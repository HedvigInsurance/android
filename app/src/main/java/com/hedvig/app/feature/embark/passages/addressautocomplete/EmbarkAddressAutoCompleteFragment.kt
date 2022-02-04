package com.hedvig.app.feature.embark.passages.addressautocomplete

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkAddressAutoCompleteActionBinding
import com.hedvig.app.feature.addressautocompletion.activityresult.FetchDanishAddressAutoCompleteContractHandler
import com.hedvig.app.feature.addressautocompletion.activityresult.FetchDanishAddressContractResult
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressStoreKey
import com.hedvig.app.feature.addressautocompletion.model.fromValueStoreKeys
import com.hedvig.app.feature.addressautocompletion.model.toValueStoreKeys
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.addressautocomplete.composables.AddressCard
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
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
        val prefilledAddress: DanishAddress? = DanishAddress.fromValueStoreKeys(embarkViewModel::getPrefillFromStore)
        parametersOf(prefilledAddress)
    }
    private val binding by viewBinding(FragmentEmbarkAddressAutoCompleteActionBinding::bind)

    private lateinit var fetchDanishAddressAutoCompleteContractHandler: FetchDanishAddressAutoCompleteContractHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchDanishAddressAutoCompleteContractHandler = FetchDanishAddressAutoCompleteContractHandler(
            registry = requireActivity().activityResultRegistry,
            onAddressResult = { result ->
                when (result) {
                    FetchDanishAddressContractResult.CantFind -> {
                        submitAddressAndProceedToNextPassage(null)
                    }
                    is FetchDanishAddressContractResult.Selected -> {
                        viewModel.updateAddressSelected(result.address)
                    }
                    else -> {
                        // no-op
                    }
                }
            }
        )
        lifecycle.addObserver(fetchDanishAddressAutoCompleteContractHandler)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.root.applyNavigationBarInsets()
        binding.messages.adapter = MessageAdapter(data.messages)
        binding.textActionSubmit.text = "Submit"
        binding.textActionSubmit
            .hapticClicks()
            .mapLatest {
                submitAddressAndProceedToNextPassage(viewModel.viewState.value.address)
            }
            .launchIn(viewLifecycleScope)

        viewModel.viewState
            .flowWithLifecycle(viewLifecycle)
            .onEach { viewState ->
                d { "Stelios: onEach: $viewState" }
                binding.inputCard.setContent {
                    HedvigTheme {
                        AddressCard(
                            addressText = viewState.address?.toPresentableText(),
                            placeholderText = data.placeholder,
                            onClick = {
                                fetchDanishAddressAutoCompleteContractHandler.startAutoCompletionActivity(
                                    initialAddress = viewState.address
                                )
                            }
                        )
                    }
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

    private fun putAddressInStore(address: DanishAddress?) {
        embarkViewModel.putInStore(data.key, null)
        DanishAddressStoreKey.clearAllStoreValues(embarkViewModel::putInStore)
        if (address == null) {
            embarkViewModel.putInStore(data.key, "ADDRESS_NOT_FOUND")
            return
        }
        embarkViewModel.putInStore(data.key, address.address)
        address.toValueStoreKeys().forEach { (key, value) ->
            embarkViewModel.putInStore(key, value)
        }
    }

    private fun submitAddressAndProceedToNextPassage(address: DanishAddress?) {
        putAddressInStore(address)
        embarkViewModel.submitAction(data.link)
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
