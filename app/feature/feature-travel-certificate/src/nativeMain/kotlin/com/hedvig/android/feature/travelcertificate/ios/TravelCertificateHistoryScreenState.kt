package com.hedvig.android.feature.travelcertificate.ios

sealed class TravelCertificateHistoryScreenState {
  data object Loading : TravelCertificateHistoryScreenState()

  data object Failed : TravelCertificateHistoryScreenState()

  /**
   * [certificates] in the order the backend lists them, possibly none. [canCreate] is whether the member may create a
   * certificate. When [mustChooseContract] is true, creating starts by choosing a contract; otherwise it starts at the
   * date step with no contract id. [addonBanner] is the travel add-on offer, when there is one.
   */
  data class Content(
    val certificates: List<TravelCertificateItem>,
    val canCreate: Boolean,
    val mustChooseContract: Boolean,
    val addonBanner: TravelAddonBanner?,
  ) : TravelCertificateHistoryScreenState()

  /** Calls the closure for this state's case, so a Swift call site stops compiling when a case is added. */
  fun handle(onLoading: () -> Unit, onFailed: () -> Unit, onContent: (Content) -> Unit) {
    when (this) {
      Loading -> onLoading()
      Failed -> onFailed()
      is Content -> onContent(this)
    }
  }
}

/**
 * [startDate] and [expiryDate] are `yyyy-MM-dd`. [isExpired] is whether [expiryDate] is today or earlier on the
 * device, as of the load. [signedUrl] opens the certificate's PDF without signing in.
 */
data class TravelCertificateItem(
  val id: String,
  val startDate: String,
  val expiryDate: String,
  val isExpired: Boolean,
  val signedUrl: String,
) {
  /** Leaves out [signedUrl]. */
  override fun toString(): String =
    "TravelCertificateItem(id=$id, startDate=$startDate, expiryDate=$expiryDate, isExpired=$isExpired)"
}

/**
 * The backend's texts for the travel add-on offer. [labels] are the badges shown with it. [eligibleContractIds] are
 * the contracts the add-on purchase flow starts with, never empty.
 */
data class TravelAddonBanner(
  val title: String,
  val bannerDescription: String,
  val labels: List<String>,
  val eligibleContractIds: List<String>,
)
