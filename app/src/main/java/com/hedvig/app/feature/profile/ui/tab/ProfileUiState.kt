package com.hedvig.app.feature.profile.ui.tab

import androidx.annotation.StringRes
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.fragment.CashbackFragment

data class ProfileUiState(
  val member: Member,
  val contactInfoName: String,
  val charityState: CharityState,
  val paymentState: PaymentState,
  val cashbackUiState: CashbackUiState?,
  val charityOptions: List<CharityOption>,
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

sealed interface CharityState {
  data class Show(val charityName: String?) : CharityState
  object DontShow : CharityState
}

sealed interface PaymentState {
  data class Show(
    val monetaryMonthlyNet: String,
    @StringRes val priceCaptionResId: Int?,
  ) : PaymentState

  object DontShow : PaymentState
}

data class CashbackUiState(
  val id: String?,
  val imageUrl: String?,
  val name: String?,
  val description: String?,
) {
  companion object {
    fun fromDto(cashbackFragment: CashbackFragment?): CashbackUiState? {
      if (cashbackFragment == null) return null
      return CashbackUiState(
        id = cashbackFragment.id,
        imageUrl = cashbackFragment.imageUrl,
        name = cashbackFragment.name,
        description = cashbackFragment.description,
      )
    }
  }
}

data class CharityOption(
  val id: String?,
  val name: String?,
  val description: String?,
) {
  companion object {
    fun fromDto(dto: ProfileQuery.CashbackOption): CharityOption {
      return CharityOption(
        id = dto.id,
        name = dto.name,
        description = dto.description,
      )
    }
  }
}
