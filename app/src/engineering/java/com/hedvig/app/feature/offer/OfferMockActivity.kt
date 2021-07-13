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
            MockOfferViewModel.apply {
                mockData = OFFER_DATA_SWEDISH_APARTMENT
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Swedish Apartment + Previous Insurer, Non-Switchable") {
            MockOfferViewModel.apply {
                mockData = OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Swedish Apartment + Previous Insurer, Switchable") {
            MockOfferViewModel.apply {
                mockData = OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Swedish House") {
            MockOfferViewModel.apply {
                mockData = OFFER_DATA_SWEDISH_HOUSE
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Swedish House with added discount") {
            MockOfferViewModel.apply {
                mockData = OFFER_DATA_SWEDISH_HOUSE_WITH_DISCOUNT
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Norway, Home Contents + Travel") {
            MockOfferViewModel.apply {
                mockData = OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Norway, Home Contents + Travel, Both with Previous Insurer, All Non-Switchable") {
            MockOfferViewModel.apply {
                mockData = OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_NONSWITCHABLE
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Norway, Home Contents + Travel, Both with Previous Insurer, All Switchable") {
            MockOfferViewModel.apply {
                mockData = OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_SWITCHABLE
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Norway, Home Contents + Travel, Both with Previous Insurer, Mixed Switchable") {
            MockOfferViewModel.apply {
                mockData = OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_MIXED_SWITCHABLE
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Bundle with concurrent inception dates") {
            MockOfferViewModel.apply {
                mockData = BUNDLE_WITH_CONCURRENT_INCEPTION_DATES
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Bundle with independent inception dates") {
            MockOfferViewModel.apply {
                mockData = BUNDLE_WITH_INDEPENDENT_INCEPTION_DATES
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Bundle with start date from previous insurer") {
            MockOfferViewModel.apply {
                mockData = BUNDLE_WITH_START_DATE_FROM_PREVIOUS_INSURER
                shouldError = false
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Error") {
            MockOfferViewModel.apply {
                shouldError = true
            }
            startActivity(OfferActivity.newInstance(context))
        }
        clickableItem("Offer with approve sign method") {
            MockOfferViewModel.mockData = BUNDLE_WITH_APPROVE
            startActivity(OfferActivity.newInstance(context))
        }
    }
}
