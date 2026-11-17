@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.travelcertificate.ios

import androidx.lifecycle.ViewModelProvider
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractEvent
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractForCertificateViewModel
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractUiState
import com.hedvig.android.molecule.public.ScreenModel
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
fun createTravelCertificateChooseContractScreenModel(
  viewModelFactory: ViewModelProvider.Factory,
): TravelCertificateChooseContractScreenModel = TravelCertificateChooseContractScreenModel(viewModelFactory)

/**
 * The contracts a certificate can be created for, when there is more than one. The chosen contract's id goes to the
 * date step. The Swift owner's rules are on [ScreenModel].
 */
class TravelCertificateChooseContractScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) :
  ScreenModel<TravelCertificateChooseContractScreenState>() {
    private val viewModel =
      viewModel(viewModelFactory, ChooseContractForCertificateViewModel::class) { it.toScreenState() }

    /** Loads the contracts again after Failed. Does nothing once Content is shown. Safe to call from any thread. */
    fun reload() {
      viewModel.send(ChooseContractEvent.RetryLoadData)
    }
  }

sealed class TravelCertificateChooseContractScreenState {
  data object Loading : TravelCertificateChooseContractScreenState()

  data object Failed : TravelCertificateChooseContractScreenState()

  /** [contracts] in the order the backend lists them. */
  data class Content(val contracts: List<TravelCertificateContract>) : TravelCertificateChooseContractScreenState()

  /** Calls the closure for this state's case, so a Swift call site stops compiling when a case is added. */
  fun handle(onLoading: () -> Unit, onFailed: () -> Unit, onContent: (Content) -> Unit) {
    when (this) {
      Loading -> onLoading()
      Failed -> onFailed()
      is Content -> onContent(this)
    }
  }
}

/** [address] is the insured street, or the contract's display name when the backend has no street for it. */
data class TravelCertificateContract(val id: String, val address: String) {
  /** Leaves out [address]. */
  override fun toString(): String = "TravelCertificateContract(id=$id)"
}

internal fun ChooseContractUiState.toScreenState(): TravelCertificateChooseContractScreenState = when (this) {
  ChooseContractUiState.Loading -> TravelCertificateChooseContractScreenState.Loading

  ChooseContractUiState.Failure -> TravelCertificateChooseContractScreenState.Failed

  is ChooseContractUiState.Success -> TravelCertificateChooseContractScreenState.Content(
    contracts = eligibleContracts.map { TravelCertificateContract(id = it.contractId, address = it.address) },
  )
}
