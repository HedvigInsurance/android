@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.travelcertificate.ios

import com.hedvig.android.feature.travelcertificate.ui.generatewhen.EmailError
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputEvent
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputUiState
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputViewModelFactory
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.ScreenModel
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/** Null and "" as [contractId] are the same: the first contract a certificate can be created for. */
@HiddenFromObjC
fun createTravelCertificateDateInputScreenModel(
  viewModelFactory: MetroViewModelFactory,
  contractId: String?,
): TravelCertificateDateInputScreenModel = TravelCertificateDateInputScreenModel(viewModelFactory, contractId)

/**
 * The email and travel date step. Submitting either offers the travellers step in `next`, or, for a member without
 * co-insured, creates the certificate. The Swift owner's rules are on [ScreenModel], and two more apply here:
 *
 * - Keep collecting [states] while `isCreating` is true. The presenter stops 5 seconds after the last collection is
 *   cancelled, which cancels the create's request; the certificate may still have been created, but this model then
 *   shows `createFailed` instead of Created, and a retry creates a second one.
 * - Call [setEmail] with the field's current text right before [submit] whenever the field's last edit may not have
 *   been sent, such as when edits are debounced or one was made while [states] was not collected. Both calls go
 *   through the same ordered event buffer, so the text is applied before the submit validates it.
 *
 * Every event function is safe to call from any thread. Events sent while [states] is not collected wait in a
 * buffer of 20, and further ones are dropped and logged, so send them only while collecting.
 */
class TravelCertificateDateInputScreenModel internal constructor(
  viewModelFactory: MetroViewModelFactory,
  contractId: String?,
) : ScreenModel<TravelCertificateDateInputScreenState>() {
  private val viewModel = viewModel(
    TravelCertificateDateInputViewModel::class,
    {
      viewModelFactory.createManuallyAssistedFactory(TravelCertificateDateInputViewModelFactory::class)()
        .create(contractId?.takeIf { it.isNotEmpty() })
    },
  ) { it.toScreenState() }

  /** Loads the form again after Failed. Does nothing while the form or Created is shown. */
  fun reload() {
    viewModel.send(TravelCertificateDateInputEvent.RetryLoadData)
  }

  /** Sets the email to [text] and clears the email error. Ignored until the form is shown. */
  fun setEmail(text: String) {
    viewModel.send(TravelCertificateDateInputEvent.ChangeEmailInput(text))
  }

  /** Picks the travel date. Ignored when [isoDate] is not a `yyyy-MM-dd` day or is outside `minDate`..`maxDate`. */
  fun selectTravelDate(isoDate: String) {
    val event = selectTravelDateEvent(isoDate)
    if (event == null) {
      logcat(LogPriority.WARN) { "TravelCertificateDateInputScreenModel ignored a date that is not yyyy-MM-dd" }
      return
    }
    viewModel.send(event)
  }

  /**
   * Validates the email and the date, then offers the travellers step or creates the certificate. Ignored while
   * creating and while `next` is set.
   */
  fun submit() {
    viewModel.send(TravelCertificateDateInputEvent.Submit)
  }

  /** Tells Kotlin the step in `next` is on screen, which clears `next`. */
  fun stepShown() {
    viewModel.send(TravelCertificateDateInputEvent.StepShown)
  }
}

/** The event [TravelCertificateDateInputScreenModel.selectTravelDate] sends for [isoDate], null when it sends none. */
internal fun selectTravelDateEvent(isoDate: String): TravelCertificateDateInputEvent.SelectTravelDate? =
  parseIsoDateOrNull(isoDate)?.let { TravelCertificateDateInputEvent.SelectTravelDate(it) }

internal fun TravelCertificateDateInputUiState.toScreenState(): TravelCertificateDateInputScreenState = when (this) {
  TravelCertificateDateInputUiState.Loading -> TravelCertificateDateInputScreenState.Loading

  TravelCertificateDateInputUiState.Failure -> TravelCertificateDateInputScreenState.Failed

  is TravelCertificateDateInputUiState.Success -> TravelCertificateDateInputScreenState.Content(
    email = email,
    emailRevision = emailRevision,
    emailError = when (emailError) {
      EmailError.Empty -> TravelCertificateEmailError.EMPTY
      EmailError.Invalid -> TravelCertificateEmailError.INVALID
      null -> null
    },
    minDate = minDate.toIsoDateString(),
    maxDate = maxDate.toIsoDateString(),
    selectedDate = selectedDate.toIsoDateString(),
    daysValid = daysValid,
    isCreating = isCreating,
    createFailed = createFailed,
    next = next,
  )

  is TravelCertificateDateInputUiState.Created -> TravelCertificateDateInputScreenState.Created(url.toCreated())
}
