package com.hedvig.android.feature.insurances.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.hedvig.android.core.ui.insurance.ProductVariant
import com.hedvig.android.core.ui.insurance.toDrawableRes
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class InsuranceContract(
  val id: String,
  val displayName: String,
  val exposureDisplayName: String,
  val inceptionDate: LocalDate,
  val terminationDate: LocalDate?,
  val currentAgreement: Agreement,
  val upcomingAgreement: Agreement?,
  val renewalDate: LocalDate?,
  val supportsAddressChange: Boolean,
  val isTerminated: Boolean,
)

data class Agreement(
  val activeFrom: LocalDate,
  val activeTo: LocalDate,
  val displayItems: List<DisplayItem>,
  val productVariant: ProductVariant,
) {
  data class DisplayItem(
    val title: String,
    val value: String,
  )
}

fun InsuranceContract.createChips(context: Context): ImmutableList<String> {
  val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
  return listOfNotNull(
    terminationDate?.let { terminationDate ->
      if (terminationDate == today) {
        context.getString(R.string.CONTRACT_STATUS_TERMINATED_TODAY)
      } else if (terminationDate < today) {
        context.getString(R.string.CONTRACT_STATUS_TERMINATED)
      } else {
        context.getString(R.string.CONTRACT_STATUS_TO_BE_TERMINATED, terminationDate)
      }
    },
    upcomingAgreement?.activeFrom?.let { activeFromDate ->
      context.getString(R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_UPDATE_DATE, activeFromDate)
    },
    inceptionDate.let { inceptionDate ->
      if (inceptionDate > today) {
        context.getString(R.string.CONTRACT_STATUS_ACTIVE_IN_FUTURE, inceptionDate)
      } else {
        null
      }
    },
  ).toPersistentList()
}

@Composable
fun InsuranceContract.createPainter(): Painter {
  return if (isTerminated) {
    ColorPainter(Color.Black.copy(alpha = 0.7f))
  } else {
    currentAgreement.productVariant.contractType
      .toDrawableRes()
      .let { drawableRes -> painterResource(id = drawableRes) }
  }
}
