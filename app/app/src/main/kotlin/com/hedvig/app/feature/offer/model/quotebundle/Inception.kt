package com.hedvig.app.feature.offer.model.quotebundle

import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.changestartdate.toChangeDateBottomSheetData
import giraffe.fragment.QuoteBundleFragment
import giraffe.type.QuoteBundleAppConfigurationStartDateTerminology

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
