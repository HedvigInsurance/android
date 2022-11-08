package com.hedvig.app.feature.embark.passages.previousinsurer

import android.os.Parcelable
import com.hedvig.android.apollo.graphql.EmbarkStoryQuery
import kotlinx.parcelize.Parcelize

@Parcelize
data class PreviousInsurerParameter(
  val messages: List<String>,
  val next: String,
  val skip: String,
  val storeKey: String,
  val previousInsurers: List<PreviousInsurer>,
) : Parcelable {
  @Parcelize
  data class PreviousInsurer(
    val name: String,
    val icon: String,
    val id: String,
    val collectionId: String?,
  ) : Parcelable

  companion object {
    fun from(
      messages: List<String>,
      previousInsuranceAction: EmbarkStoryQuery.AsEmbarkPreviousInsuranceProviderAction,
    ) =
      PreviousInsurerParameter(
        messages = messages,
        next = previousInsuranceAction.previousInsurerData.next.fragments.embarkLinkFragment.name,
        skip = previousInsuranceAction.previousInsurerData.skip.fragments.embarkLinkFragment.name,
        previousInsurers = previousInsuranceAction
          .previousInsurerData
          .insuranceProviders
          .map {
            PreviousInsurer(
              it.name,
              it.logo.variants.fragments.iconVariantsFragment.light.svgUrl,
              it.id,
              null,
            )
          },
        storeKey = previousInsuranceAction.previousInsurerData.storeKey,
      )
  }
}
