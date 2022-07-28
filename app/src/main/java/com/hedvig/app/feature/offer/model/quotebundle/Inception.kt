package com.hedvig.app.feature.offer.model.quotebundle

import com.hedvig.android.owldroid.graphql.fragment.QuoteBundleFragment
import com.hedvig.android.owldroid.graphql.type.QuoteBundleAppConfigurationStartDateTerminology
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.changestartdate.toChangeDateBottomSheetData

data class Inception(
  val startDate: OfferStartDate,
  val startDateLabel: StartDateLabel,
  val changeDateData: ChangeDateBottomSheetData,
)

fun QuoteBundleFragment.Inception1.toInception(
  startDateTerminology: QuoteBundleAppConfigurationStartDateTerminology,
  quoteCartId: QuoteCartId,
  quoteNames: List<String>,
) = Inception(
  startDate = getStartDate(),
  startDateLabel = getStartDateLabel(startDateTerminology),
  changeDateData = toChangeDateBottomSheetData(quoteCartId, quoteNames),
)
