@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.travelcertificate.ios

import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputEvent
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputViewModelFactory
import com.hedvig.android.molecule.public.ScreenModel
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
fun createTravelCertificateTravellersScreenModel(
  viewModelFactory: MetroViewModelFactory,
  step: TravelCertificateTravellersStep,
): TravelCertificateTravellersScreenModel = TravelCertificateTravellersScreenModel(viewModelFactory, step)

/**
 * Choosing who travels, then creating the certificate. The Swift owner's rules are on [ScreenModel], and one more
 * applies here: do not let the screen be dismissed while `isCreating` is true: disable back and interactive
 * dismissal. Closing the model cancels the create's request at once, the certificate may still have been created,
 * and nothing shows the outcome, so if the screen goes away anyway, reload the history. Collection must not stop
 * while `isCreating` is true either: the presenter stops 5 seconds after the last collection is cancelled, which
 * cancels the request too.
 *
 * Every event function is safe to call from any thread. Events sent while [states] is not collected wait in a
 * buffer of 20, and further ones are dropped and logged, so send them only while collecting.
 */
class TravelCertificateTravellersScreenModel internal constructor(
  viewModelFactory: MetroViewModelFactory,
  step: TravelCertificateTravellersStep,
) : ScreenModel<TravelCertificateTravellersScreenState>() {
  private val viewModel = viewModel(
    TravelCertificateTravellersInputViewModel::class,
    {
      viewModelFactory.createManuallyAssistedFactory(TravelCertificateTravellersInputViewModelFactory::class)()
        .create(step)
    },
  ) { it.toScreenState() }

  /** Loads the co-insured again, keeping the shown state until they arrive. Ignored after Created. */
  fun reload() {
    viewModel.send(TravelCertificateTravellersInputEvent.RetryLoadData)
  }

  /** Includes or leaves out the member. Ignored while creating and after Created. */
  fun setMemberIncluded(included: Boolean) {
    viewModel.send(TravelCertificateTravellersInputEvent.SetMemberIncluded(included))
  }

  /**
   * Includes or leaves out the co-insured with [id], a `TravelCertificateTraveller.id`. Ignored for an id not in the
   * shown list, while creating and after Created.
   */
  fun setCoInsuredIncluded(id: String, included: Boolean) {
    viewModel.send(setCoInsuredIncludedEvent(id, included))
  }

  /** Creates the certificate for the included travellers. Ignored while creating and when `canCreate` is false. */
  fun create() {
    viewModel.send(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
  }
}

/** The event [TravelCertificateTravellersScreenModel.setCoInsuredIncluded] sends. */
internal fun setCoInsuredIncludedEvent(id: String, included: Boolean) =
  TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(CoInsuredId(id), included)

internal fun TravelCertificateTravellersInputUiState.toScreenState(): TravelCertificateTravellersScreenState =
  when (this) {
    TravelCertificateTravellersInputUiState.Loading -> TravelCertificateTravellersScreenState.Loading

    TravelCertificateTravellersInputUiState.Failure -> TravelCertificateTravellersScreenState.Failed

    is TravelCertificateTravellersInputUiState.Success -> TravelCertificateTravellersScreenState.Content(
      memberFullName = memberFullName,
      isMemberIncluded = isMemberIncluded,
      coInsured = coInsuredList.map { it.toTraveller() },
      coInsuredHasMissingInfo = coInsuredHasMissingInfo,
      canCreate = canCreate,
      isCreating = isCreating,
      createFailed = createFailed,
    )

    is TravelCertificateTravellersInputUiState.Created -> TravelCertificateTravellersScreenState.Created(
      exit.map { it.toCreated() },
    )
  }

private fun CoInsured.toTraveller(): TravelCertificateTraveller = TravelCertificateTraveller(
  id = id.value,
  name = name,
  dateOfBirth = dateOfBirth?.toIsoDateString(),
  isIncluded = isIncluded,
)
