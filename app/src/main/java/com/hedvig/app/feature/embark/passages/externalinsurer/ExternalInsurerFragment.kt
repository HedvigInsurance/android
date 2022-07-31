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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.app.R
import com.hedvig.app.databinding.PreviousOrExternalInsurerFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.AskForPriceInfoActivity
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.AskForPriceInfoActivity.Companion.RESULT_CONTINUE
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.InsuranceProviderParameter
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity.Companion.REFERENCE_RESULT
import com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice.RetrievePriceInfoActivity.Companion.SSN_RESULT
import com.hedvig.app.feature.embark.passages.previousinsurer.InsurerProviderBottomSheet
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerParameter
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.android.core.common.android.whenApiVersion
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.launch
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
        result.data?.getStringExtra(SSN_RESULT)?.let {
          embarkViewModel.putInStore("personalNumber", it)
        }
        continueEmbark()
      }
    }

  private val insurerData by lazy {
    requireArguments()
      .getParcelable<ExternalInsurerParameter>(DATA)
      ?: throw IllegalArgumentException("No argument passed to ${this.javaClass.name}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    postponeEnterTransition()

    viewLifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.events.collect { event ->
            when (event) {
              is ExternalInsurerViewModel.Event.Error -> context?.showErrorDialog(
                message = getString(event.errorResult.getStringRes()),
                positiveAction = {},
              )
              is ExternalInsurerViewModel.Event.AskForPrice -> {
                startAskForPrice(event.collectionId, event.providerName)
              }
              ExternalInsurerViewModel.Event.CantAutomaticallyMoveInsurance -> {
                MaterialAlertDialogBuilder(requireContext())
                  .setTitle(getString(hedvig.resources.R.string.EXTERNAL_INSURANCE_PROVIDER_ALERT_TITLE))
                  .setMessage(getString(hedvig.resources.R.string.EXTERNAL_INSURANCE_PROVIDER_ALERT_MESSAGE))
                  .setPositiveButton(getString(hedvig.resources.R.string.ALERT_OK)) { _, _ -> continueEmbark() }
                  .show()
              }
              ExternalInsurerViewModel.Event.SkipDataCollection -> continueEmbark()
            }
          }
        }
        viewModel.viewState.collect { viewState ->
          binding.progress.isVisible = viewState.isLoading

          viewState.selectedProvider?.let {
            binding.currentInsurerLabel.text = it.name
          }

          binding.currentInsurerContainer.setOnClickListener {
            viewState.insuranceProviders?.let(::showInsurers)
          }

          binding.continueButton.isEnabled = viewState.canContinue()
          binding.continueButton.setOnClickListener {
            viewState.selectedProvider?.let { selectedInsuranceProvider ->
              viewModel.continueWithProvider(selectedInsuranceProvider, resources)
            }
          }
        }
      }
    }

    with(binding) {
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
    }
    setFragmentResultListener(InsurerProviderBottomSheet.REQUEST_KEY) { _: String, bundle: Bundle ->
      handleInsurerProviderBottomSheetResult(bundle)
    }
  }

  private fun handleInsurerProviderBottomSheetResult(bundle: Bundle) {
    val id = bundle.getString(InsurerProviderBottomSheet.INSURER_ID_KEY)
      ?: throw IllegalArgumentException("Id not found in bundle from InsurerProviderBottomSheet")
    val collectionId = bundle.getString(InsurerProviderBottomSheet.INSURER_COLLECTION_ID_KEY)
      ?: throw IllegalArgumentException("Collection Id not found in bundle from InsurerProviderBottomSheet")
    val name = bundle.getString(InsurerProviderBottomSheet.INSURER_NAME_KEY)
      ?: throw IllegalArgumentException("Name not found in bundle from InsurerProviderBottomSheet")
    embarkViewModel.putInStore(insurerData.storeKey, id)
    viewModel.selectInsuranceProvider(
      InsuranceProvider(
        id = id,
        collectionId = collectionId,
        name = name,
      ),
    )
  }

  private fun startAskForPrice(collectionId: String, name: String) {
    val intent = AskForPriceInfoActivity.createIntent(
      requireContext(),
      InsuranceProviderParameter(collectionId, name),
    )
    askForPriceActivityResultLauncher.launch(intent)
  }

  private fun showInsurers(insuranceProviders: List<InsuranceProvider>) {
    val fragment = InsurerProviderBottomSheet.newInstance(
      insuranceProviders.map {
        PreviousInsurerParameter.PreviousInsurer(
          name = it.name,
          icon = "",
          id = it.id,
          collectionId = it.collectionId,
        )
      },
    )
    fragment.show(parentFragmentManager, InsurerProviderBottomSheet.TAG)
  }

  private fun continueEmbark() {
    embarkViewModel.submitAction(insurerData.next)
  }

  private fun InsuranceProvidersResult.Error.getStringRes() = when (this) {
    InsuranceProvidersResult.Error.NetworkError -> hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE
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
