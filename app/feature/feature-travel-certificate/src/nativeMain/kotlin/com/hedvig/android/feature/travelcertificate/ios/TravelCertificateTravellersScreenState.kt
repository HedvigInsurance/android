package com.hedvig.android.feature.travelcertificate.ios

import com.hedvig.android.molecule.public.NavigationRequest

sealed class TravelCertificateTravellersScreenState {
  data object Loading : TravelCertificateTravellersScreenState()

  /** The co-insured could not be loaded. */
  data object Failed : TravelCertificateTravellersScreenState()

  /**
   * [coInsured] sorted by name, without the co-insured whose data is incomplete; [coInsuredHasMissingInfo] is true
   * when any was left out for that reason. The list is refreshed every few seconds while collected, and the selection
   * survives a refresh. [canCreate] is whether at least one traveller is included.
   *
   * [isCreating] is true while the certificate is being created; selection changes are ignored meanwhile.
   * [createFailed] is true after a create failed, until the next accepted create.
   */
  data class Content(
    val memberFullName: String,
    val isMemberIncluded: Boolean,
    val coInsured: List<TravelCertificateTraveller>,
    val coInsuredHasMissingInfo: Boolean,
    val canCreate: Boolean,
    val isCreating: Boolean,
    val createFailed: Boolean,
  ) : TravelCertificateTravellersScreenState() {
    /** Leaves out [memberFullName]. */
    override fun toString(): String = "Content(isMemberIncluded=$isMemberIncluded, coInsured=$coInsured, " +
      "coInsuredHasMissingInfo=$coInsuredHasMissingInfo, canCreate=$canCreate, isCreating=$isCreating, " +
      "createFailed=$createFailed)"
  }

  /**
   * The certificate exists. Nothing follows on this screen. Leave for [certificate]'s target only when its
   * `tryConsume()` returns true, which it does once, however often this state is observed.
   */
  data class Created(val certificate: NavigationRequest<TravelCertificateCreated>) :
    TravelCertificateTravellersScreenState()

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

/**
 * A co-insured person. [id] identifies them for `setCoInsuredIncluded` and carries none of their data. [dateOfBirth]
 * is `yyyy-MM-dd`, or null when the backend has none.
 */
data class TravelCertificateTraveller(
  val id: String,
  val name: String,
  val dateOfBirth: String?,
  val isIncluded: Boolean,
) {
  /** Leaves out [id], [name] and [dateOfBirth]. */
  override fun toString(): String = "TravelCertificateTraveller(isIncluded=$isIncluded)"
}
