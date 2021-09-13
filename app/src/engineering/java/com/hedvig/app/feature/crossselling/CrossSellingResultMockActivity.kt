package com.hedvig.app.feature.crossselling

import com.hedvig.app.GenericDevelopmentAdapter
import com.hedvig.app.MockActivity
import com.hedvig.app.feature.crossselling.ui.CrossSellingResult
import com.hedvig.app.feature.crossselling.ui.CrossSellingResultActivity
import com.hedvig.app.genericDevelopmentAdapter
import org.koin.core.module.Module
import java.time.LocalDate

class CrossSellingResultMockActivity : MockActivity() {
    override val original: List<Module> = listOf()
    override val mocks: List<Module> = listOf()

    override fun adapter(): GenericDevelopmentAdapter = genericDevelopmentAdapter {
        header("Cross-Sell result screens")
        clickableItem("Successful Accident cross-sell starting before today") {
            startActivity(
                CrossSellingResultActivity.newInstance(
                    this@CrossSellingResultMockActivity,
                    CrossSellingResult.Success(LocalDate.now().minusDays(1)),
                )
            )
        }
        clickableItem("Successful Accident cross-sell starting in two days") {
            startActivity(
                CrossSellingResultActivity.newInstance(
                    this@CrossSellingResultMockActivity,
                    CrossSellingResult.Success(LocalDate.now().plusDays(2)),
                )
            )
        }
        clickableItem("Failed") {
            startActivity(
                CrossSellingResultActivity.newInstance(
                    this@CrossSellingResultMockActivity,
                    CrossSellingResult.Error
                )
            )
        }
    }
}
