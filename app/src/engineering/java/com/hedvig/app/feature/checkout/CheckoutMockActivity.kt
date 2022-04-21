package com.hedvig.app.feature.checkout

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.offer.MockOfferViewModel
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.offerModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class CheckoutMockActivity : MockActivity() {
    override val original = listOf(offerModule)
    override val mocks = listOf(
        module {
            viewModel<OfferViewModel> { MockOfferViewModel() }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Checkout Screen")
        clickableItem("Checkout") {
            startActivity(
                CheckoutActivity.newInstance(
                    context,
                    CheckoutParameter(
                        quoteIds = listOf(""),
                        quoteCartId = null,
                    )
                )
            )
        }
    }
}
