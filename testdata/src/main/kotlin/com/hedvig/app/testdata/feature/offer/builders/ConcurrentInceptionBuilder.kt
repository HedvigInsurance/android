package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.apollo.graphql.fragment.CurrentInsurerFragment
import com.hedvig.android.apollo.graphql.fragment.QuoteBundleFragment
import com.hedvig.android.apollo.graphql.type.ConcurrentInception
import java.time.LocalDate

class ConcurrentInceptionBuilder(
  val quoteIds: List<String> = listOf("ea656f5f-40b2-4953-85d9-752b33e69e38"),
  val startDate: LocalDate? = LocalDate.now(),
  val currentInsurer: QuoteBundleFragment.CurrentInsurer1? = QuoteBundleFragment.CurrentInsurer1(
    __typename = "",
    fragments = QuoteBundleFragment.CurrentInsurer1.Fragments(
      currentInsurerFragment = CurrentInsurerFragment(
        id = "currentinsurerid",
        displayName = "Test current insurer",
        switchable = false,
      ),
    ),
  ),
) {
  fun build() = QuoteBundleFragment.Inception1(
    __typename = ConcurrentInception.type.name,
    asConcurrentInception = QuoteBundleFragment.AsConcurrentInception(
      __typename = ConcurrentInception.type.name,
      correspondingQuoteIds = quoteIds,
      startDate = startDate,
      currentInsurer = currentInsurer,
    ),
    asIndependentInceptions = null,
  )
}
