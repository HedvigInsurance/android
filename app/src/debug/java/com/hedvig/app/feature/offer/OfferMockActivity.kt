package com.hedvig.app.feature.offer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.GenericDevelopmentAdapter
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.offerModule
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
import kotlinx.android.synthetic.debug.activity_generic_development.*
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class OfferMockActivity : AppCompatActivity(R.layout.activity_generic_development) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unloadKoinModules(offerModule)
        loadKoinModules(mockOfferModule)

        root.adapter = GenericDevelopmentAdapter(
            listOf(
                GenericDevelopmentAdapter.Item.Header("Offer Screen"),
                GenericDevelopmentAdapter.Item.ClickableItem("Swedish Apartment, No previous insurer") {
                    MockOfferViewModel.mockData = OFFER_DATA_SWEDISH_APARTMENT
                    startActivity(OfferActivity.newInstance(this))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Swedish Apartment, Previous insurer, Switchable") {
                    MockOfferViewModel.mockData =
                        OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
                    startActivity(OfferActivity.newInstance(this))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Swedish Apartment, Previous insurer, Non-Switchable") {
                    MockOfferViewModel.mockData =
                        OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE
                    startActivity(OfferActivity.newInstance(this))
                }
            )
        )
    }

    companion object {
        val mockOfferModule = module {
            viewModel<OfferViewModel> { MockOfferViewModel() }
        }
    }
}
