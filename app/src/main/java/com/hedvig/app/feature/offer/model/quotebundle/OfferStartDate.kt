package com.hedvig.app.feature.offer.model.quotebundle

import android.content.Context
import com.hedvig.android.owldroid.graphql.fragment.QuoteBundleFragment
import com.hedvig.app.R
import com.hedvig.app.feature.embark.masking.ISO_8601_DATE
import com.hedvig.app.util.extensions.isToday
import java.time.LocalDate

sealed class OfferStartDate {
  object WhenCurrentPlanExpires : OfferStartDate()
  object Multiple : OfferStartDate()
  data class AtDate(val date: LocalDate) : OfferStartDate()
}

fun OfferStartDate.getString(context: Context): String? = when (this) {
  is OfferStartDate.AtDate -> if (date.isToday()) {
    context.getString(R.string.START_DATE_TODAY)
  } else {
    date.format(ISO_8601_DATE)
  }
  OfferStartDate.Multiple -> context.getString(R.string.OFFER_START_DATE_MULTIPLE)
  OfferStartDate.WhenCurrentPlanExpires -> context.getString(R.string.START_DATE_EXPIRES)
}

fun QuoteBundleFragment.Inception1.getStartDate() = when {
  isSwitcher() && hasNoDate() -> OfferStartDate.WhenCurrentPlanExpires
  hasNoDate() -> OfferStartDate.AtDate(LocalDate.now())
  asConcurrentInception != null -> OfferStartDate.AtDate(asConcurrentInception?.startDate ?: LocalDate.now())
  asIndependentInceptions != null -> {
    val inception = asIndependentInceptions?.inceptions?.firstOrNull()
    val allStartDatesEqual = asIndependentInceptions?.inceptions?.all { it.startDate == inception?.startDate }
    if (allStartDatesEqual == true) {
      OfferStartDate.AtDate(inception?.startDate ?: LocalDate.now())
    } else {
      OfferStartDate.Multiple
    }
  }
  else -> throw IllegalArgumentException("Could not parse inception")
}

private fun QuoteBundleFragment.Inception1.hasNoDate(): Boolean {
  return (asConcurrentInception != null && asConcurrentInception?.startDate == null) ||
    (asIndependentInceptions != null && asIndependentInceptions?.inceptions?.all { it.startDate == null } == true)
}

private fun QuoteBundleFragment.Inception1.isSwitcher(): Boolean {
  return (
    asIndependentInceptions?.inceptions?.all {
      it.currentInsurer?.fragments?.currentInsurerFragment?.switchable == true
    } == true
    ) ||
    (asConcurrentInception?.currentInsurer?.fragments?.currentInsurerFragment?.switchable == true)
}
