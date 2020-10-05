package com.hedvig.app.feature.offer

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.offerModule
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class OfferMockActivity : MockActivity() {
    override val original = listOf(offerModule)
    override val mocks = listOf(module {
        viewModel<OfferViewModel> { MockOfferViewModel() }
    })

    override fun adapter() = genericDevelopmentAdapter {
        header("Offer Screen")
        clickableItem("Swedish Apartment, No previous insurer") {
            MockOfferViewModel.mockData = OFFER_DATA_SWEDISH_APARTMENT
            startActivity(OfferActivity.newInstance(this@OfferMockActivity))
        }
        clickableItem("Swedish Apartment, Previous insurer, Switchable") {
            MockOfferViewModel.mockData =
                OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
            startActivity(OfferActivity.newInstance(this@OfferMockActivity))
        }
        clickableItem("Swedish Apartment, Previous insurer, Non-Switchable") {
            MockOfferViewModel.mockData =
                OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE
            startActivity(OfferActivity.newInstance(this@OfferMockActivity))
        }
    }
}
