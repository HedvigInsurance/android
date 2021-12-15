package com.hedvig.app.feature.embark.passages.externalinsurer

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.app.R
import com.hedvig.app.databinding.PreviousOrExternalInsurerFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.AskForPriceInfoActivity
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.AskForPriceInfoActivity.Companion.RESULT_CONTINUE
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.InsuranceProviderParameter
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity.Companion.REFERENCE_RESULT
import com.hedvig.app.feature.embark.passages.previousinsurer.InsurerProviderBottomSheet
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerParameter
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.whenApiVersion
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ExternalInsurerFragment : Fragment(R.layout.previous_or_external_insurer_fragment) {

    private val binding by viewBinding(PreviousOrExternalInsurerFragmentBinding::bind)

    private val embarkViewModel: EmbarkViewModel by sharedViewModel()
    private val viewModel: ExternalInsurerViewModel by sharedViewModel()

    private val askForPriceActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_CONTINUE) {
                result.data?.getStringExtra(REFERENCE_RESULT)?.let {
                    embarkViewModel.putInStore("dataCollectionId", it)
                }
                continueEmbark()
            }
        }

    private val insurerData by lazy {
        requireArguments()
            .getParcelable<ExternalInsurerParameter>(DATA)
            ?: throw IllegalArgumentException("No argument passed to ${this.javaClass.name}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.events
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    is ExternalInsurerViewModel.Event.Error -> context?.showErrorDialog(
                        getString(event.errorResult.getStringRes())
                    ) {}
                }
            }
            .launchIn(lifecycleScope)

        viewModel.viewState
            .flowWithLifecycle(lifecycle)
            .onEach { viewState ->
                binding.progress.isVisible = viewState.isLoading

                viewState.selectedProvider?.let {
                    binding.currentInsurerLabel.text = it.name
                }

                binding.currentInsurerContainer.setOnClickListener {
                    viewState.insuranceProviders?.let(::showInsurers)
                }

                binding.continueButton.isEnabled = viewState.canContinue()
                binding.continueButton.setOnClickListener {
                    viewState.selectedProvider?.let {
                        continueWithProvider(it)
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.apply {
            whenApiVersion(Build.VERSION_CODES.R) {
                currentInsurerContainer.setupInsetsForIme(
                    root = root,
                    currentInsurerContainer,
                )
            }

            messages.adapter = MessageAdapter(insurerData.messages)

            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }

            setFragmentResultListener(InsurerProviderBottomSheet.REQUEST_KEY) { _: String, bundle: Bundle ->
                handleInsurerProviderBottomSheetResult(bundle)
            }
        }
    }

    private fun handleInsurerProviderBottomSheetResult(bundle: Bundle) {
        val id = bundle.getString(InsurerProviderBottomSheet.INSURER_ID_KEY)
            ?: throw IllegalArgumentException("Id not found in bundle from InsurerProviderBottomSheet")
        val name = bundle.getString(InsurerProviderBottomSheet.INSURER_NAME_KEY)
            ?: throw IllegalArgumentException("Name not found in bundle from InsurerProviderBottomSheet")
        embarkViewModel.putInStore(insurerData.storeKey, id)
        viewModel.selectInsuranceProvider(
            InsuranceProvider(
                id = id,
                collectionId = id,
                name = name
            )
        )
    }

    private fun startAskForPrice(collectionId: String, name: String) {
        val intent = AskForPriceInfoActivity.createIntent(
            requireContext(),
            InsuranceProviderParameter(collectionId, name)
        )
        askForPriceActivityResultLauncher.launch(intent)
    }

    private fun showInsurers(insuranceProviders: List<InsuranceProvider>) {
        val fragment = InsurerProviderBottomSheet.newInstance(
            insuranceProviders.map {
                PreviousInsurerParameter.PreviousInsurer(it.name, "", it.collectionId ?: "")
            }
        )
        fragment.show(parentFragmentManager, InsurerProviderBottomSheet.TAG)
    }

    private fun continueWithProvider(provider: InsuranceProvider) {
        if (provider.collectionId == getString(R.string.EXTERNAL_INSURANCE_PROVIDER_OTHER_OPTION) ||
            provider.collectionId == null
        ) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.EXTERNAL_INSURANCE_PROVIDER_ALERT_TITLE))
                .setMessage(getString(R.string.EXTERNAL_INSURANCE_PROVIDER_ALERT_MESSAGE))
                .setPositiveButton(getString(R.string.ALERT_OK)) { _, _ -> continueEmbark() }
                .show()
        } else {
            startAskForPrice(provider.collectionId, provider.name)
        }
    }

    private fun continueEmbark() {
        embarkViewModel.submitAction(insurerData.next)
    }

    private fun InsuranceProvidersResult.Error.getStringRes() = when (this) {
        InsuranceProvidersResult.Error.NetworkError -> R.string.NETWORK_ERROR_ALERT_MESSAGE
    }

    companion object {
        private const val DATA = "DATA"

        fun newInstance(previousInsurerData: ExternalInsurerParameter) =
            ExternalInsurerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, previousInsurerData)
                }
            }
    }
}
