package com.hedvig.android.feature.travelcertificate.ios

import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.molecule.public.NavigationRequest

sealed class TravelCertificateDateInputScreenState {
  data object Loading : TravelCertificateDateInputScreenState()

  /** Loading failed, or the member has no contract a certificate can be created for. */
  data object Failed : TravelCertificateDateInputScreenState()

  /**
   * The form. All dates are `yyyy-MM-dd` calendar days: show each as that year, month and day in the picker's own
   * calendar, never through a GMT-based formatter such as `ISO8601DateFormatter`.
   *
   * [email] is the text Kotlin holds. [emailRevision] changes only when Kotlin writes [email] itself (the prefill
   * from the member's profile), never when `setEmail` is applied, so a field that owns its text copies [email] in
   * only when [emailRevision] differs from the one it last copied.
   *
   * [emailError] is set by a submit with an empty or malformed email and cleared by the next `setEmail`.
   *
   * [selectedDate] is the member's pick, or today moved into [minDate]..[maxDate] while there is none. [daysValid] is
   * how many days the certificate covers.
   *
   * [hasCoInsured] says what `submit()` does once the form is valid: when false it creates the certificate at once;
   * when true it offers the travellers step in [next] instead, and nothing is created until that step's create.
   *
   * [isCreating] is true while a certificate is being created, which happens when [hasCoInsured] is false.
   * [createFailed] is true after a create failed, until the next accepted submit.
   *
   * [next] is the travellers step to open, after a submit by a member with co-insured. Open its target only when
   * its `tryConsume()` returns true, which it does once per request however often this state is observed. A submit
   * is ignored until the request is taken, and a later one offers a new request. The taken request stays in [next]
   * until then.
   */
  data class Content(
    val email: String,
    val emailRevision: Int,
    val emailError: TravelCertificateEmailError?,
    val minDate: String,
    val maxDate: String,
    val selectedDate: String,
    val daysValid: Int,
    val hasCoInsured: Boolean,
    val isCreating: Boolean,
    val createFailed: Boolean,
    val next: NavigationRequest<TravelCertificateTravellersStep>?,
  ) : TravelCertificateDateInputScreenState() {
    /** Leaves out [email]. */
    override fun toString(): String = "Content(emailRevision=$emailRevision, emailError=$emailError, " +
      "minDate=$minDate, maxDate=$maxDate, selectedDate=$selectedDate, daysValid=$daysValid, " +
      "hasCoInsured=$hasCoInsured, isCreating=$isCreating, createFailed=$createFailed, next=$next)"
  }

  /** The certificate exists. Nothing follows on this screen. */
  data class Created(val certificate: TravelCertificateCreated) : TravelCertificateDateInputScreenState()

  /** Calls the closure for this state's case, so a Swift call site stops compiling when a case is added. */
  fun handle(onLoading: () -> Unit, onFailed: () -> Unit, onContent: (Content) -> Unit, onCreated: (Created) -> Unit) {
    when (this) {
      Loading -> onLoading()
      Failed -> onFailed()
      is Content -> onContent(this)
      is Created -> onCreated(this)
    }
  }
}

enum class TravelCertificateEmailError {
  EMPTY,
  INVALID,
  ;

  /** Calls the closure for this entry, so a Swift call site stops compiling when an entry is added. */
  fun handle(onEmpty: () -> Unit, onInvalid: () -> Unit) {
    when (this) {
      EMPTY -> onEmpty()
      INVALID -> onInvalid()
    }
  }
}
