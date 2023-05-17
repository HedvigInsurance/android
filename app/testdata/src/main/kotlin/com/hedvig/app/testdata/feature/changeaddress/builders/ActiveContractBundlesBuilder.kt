package com.hedvig.app.testdata.feature.changeaddress.builders

import giraffe.ActiveContractBundlesQuery

class ActiveContractBundlesBuilder(
  val embarkStoryId: String? = null,
) {

  fun build() = ActiveContractBundlesQuery.ActiveContractBundle(
    angelStories = ActiveContractBundlesQuery.AngelStories(
      addressChangeV2 = embarkStoryId,
    ),
  )
}
