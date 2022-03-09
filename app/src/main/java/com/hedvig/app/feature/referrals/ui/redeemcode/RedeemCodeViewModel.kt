package com.hedvig.app.feature.referrals.ui.redeemcode

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.usecase.EditCampaignUseCase
import com.hedvig.app.feature.referrals.data.RedeemReferralCodeRepository
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.launch
import timber.log.Timber

class RedeemCodeViewModel(
    private val quoteCartId: QuoteCartId?,
    private val redeemReferralCodeRepository: RedeemReferralCodeRepository,
    private val featureManager: FeatureManager,
    private val editCampaignUseCase: EditCampaignUseCase,
) : ViewModel() {

    val redeemCodeStatus: MutableLiveData<RedeemReferralCodeMutation.Data> = MutableLiveData()

    fun redeemReferralCode(code: String) {
        viewModelScope.launch {
            if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
                if (quoteCartId == null) {
                    Timber.d("Quote cart id null")
                } else {
                    editCampaignUseCase.addCampaignToQuoteCart(code, quoteCartId)
                        .tapLeft { Timber.d(it.message) }
                }
            } else {
                redeemReferralCodeRepository.redeemReferralCode(code)
                    .fold(
                        ifLeft = { Timber.e(it.message) },
                        ifRight = { redeemCodeStatus.postValue(it) }
                    )
            }
        }
    }
}
