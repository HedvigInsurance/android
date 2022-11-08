package com.hedvig.app.feature.embark.passages.externalinsurer

import android.os.Parcelable
import com.hedvig.android.apollo.graphql.EmbarkStoryQuery
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalInsurerParameter(
  val messages: List<String>,
  val next: String,
  val skip: String,
  val storeKey: String,
) : Parcelable {

  companion object {
    fun from(
      messages: List<String>,
      previousInsuranceAction: EmbarkStoryQuery.AsEmbarkExternalInsuranceProviderAction,
    ) = ExternalInsurerParameter(
      messages = messages,
      next = previousInsuranceAction.externalInsurerData.next.fragments.embarkLinkFragment.name,
      skip = previousInsuranceAction.externalInsurerData.skip.fragments.embarkLinkFragment.name,
      storeKey = previousInsuranceAction.externalInsurerData.storeKey,
    )
  }
}
