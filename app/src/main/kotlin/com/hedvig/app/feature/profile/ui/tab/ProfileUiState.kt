package com.hedvig.app.feature.profile.ui.tab

import androidx.annotation.StringRes
import giraffe.ProfileQuery

data class ProfileUiState(
  val member: Member,
  val contactInfoName: String,
  val showBusinessModel: Boolean,
  val paymentState: PaymentState,
)

data class Member(
  val email: String?,
  val phoneNumber: String?,
) {
  companion object {
    fun fromDto(dto: ProfileQuery.Member): Member {
      return Member(
        email = dto.email,
        phoneNumber = dto.phoneNumber,
      )
    }
  }
}

sealed interface PaymentState {
  data class Show(
    val monetaryMonthlyNet: String,
    @StringRes val priceCaptionResId: Int?,
  ) : PaymentState

  object DontShow : PaymentState
}
