package com.hedvig.app.feature.profile.ui.tab

import androidx.annotation.StringRes
import com.hedvig.android.owldroid.fragment.CashbackFragment
import com.hedvig.android.owldroid.graphql.ProfileQuery

data class ProfileUiState(
    val member: ProfileQuery.Member,
    val contactInfoName: String,
    val charityState: CharityState,
    val priceData: PriceData,
    val cashbackUiState: CashbackUiState?,
    val charityOptions: List<CharityOption>,
)

sealed interface CharityState {
    data class Selected(val charityName: String) : CharityState
    object DontShow : CharityState
    object NoneSelected : CharityState
}

data class PriceData(
    val monetaryMonthlyNet: String,
    @StringRes val priceCaptionResId: Int?,
)

data class CashbackUiState(
    val id: String?,
    val imageUrl: String?,
    val name: String?,
    val description: String?,
) {
    companion object {
        fun fromDto(cashbackFragment: CashbackFragment?): CashbackUiState {
            return CashbackUiState(
                id = cashbackFragment?.id,
                imageUrl = cashbackFragment?.imageUrl,
                name = cashbackFragment?.name,
                description = cashbackFragment?.description,
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
