package com.hedvig.app.feature.offer

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.offerModule
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_APPROVE
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_CONCURRENT_INCEPTION_DATES
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_INDEPENDENT_INCEPTION_DATES
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_START_DATE_FROM_PREVIOUS_INSURER
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_NONSWITCHABLE
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_SWITCHABLE
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_MIXED_SWITCHABLE
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_HOUSE
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_HOUSE_WITH_DISCOUNT
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class OfferMockActivity : MockActivity() {
    override val original = listOf(offerModule)
    override val mocks = listOf(
        module {
            viewModel<OfferViewModel> { MockOfferViewModel() }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Offer Screen")
        clickableItem("Swedish Apartment") {
            MockOfferViewModel.mockData = OFFER_DATA_SWEDISH_APARTMENT
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Swedish Apartment + Previous Insurer, Non-Switchable") {
            MockOfferViewModel.mockData = OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Swedish Apartment + Previous Insurer, Switchable") {
            MockOfferViewModel.mockData = OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Swedish House") {
            MockOfferViewModel.mockData = OFFER_DATA_SWEDISH_HOUSE
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Swedish House with added discount") {
            MockOfferViewModel.mockData = OFFER_DATA_SWEDISH_HOUSE_WITH_DISCOUNT
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Norway, Home Contents + Travel") {
            MockOfferViewModel.mockData = OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Norway, Home Contents + Travel, Both with Previous Insurer, All Non-Switchable") {
            MockOfferViewModel.mockData =
                OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_NONSWITCHABLE
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Norway, Home Contents + Travel, Both with Previous Insurer, All Switchable") {
            MockOfferViewModel.mockData =
                OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_SWITCHABLE
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Norway, Home Contents + Travel, Both with Previous Insurer, Mixed Switchable") {
            MockOfferViewModel.mockData =
                OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_MIXED_SWITCHABLE
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Bundle with concurrent inception dates") {
            MockOfferViewModel.mockData = BUNDLE_WITH_CONCURRENT_INCEPTION_DATES
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Bundle with independent inception dates") {
            MockOfferViewModel.mockData = BUNDLE_WITH_INDEPENDENT_INCEPTION_DATES
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Bundle with start date from previous insurer") {
            MockOfferViewModel.mockData = BUNDLE_WITH_START_DATE_FROM_PREVIOUS_INSURER
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Offer with approve sign method") {
            MockOfferViewModel.mockData = BUNDLE_WITH_APPROVE
            startActivity(OfferActivity.newInstance(context))
        }
    }
}
