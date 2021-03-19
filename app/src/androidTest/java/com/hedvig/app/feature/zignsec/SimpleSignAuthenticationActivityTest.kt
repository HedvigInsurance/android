package com.hedvig.app.feature.zignsec

import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class SimpleSignAuthenticationActivityTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(SimpleSignAuthenticationActivity::class.java)

    @Test
    fun shouldValidatePersonalIdentificationNumbersForDenmark() = run {
        activityRule.launch(SimpleSignAuthenticationActivity.newInstance(context(), Market.DK))

        SimpleSignAuthenticationScreen {
            signIn { isDisabled() }
            input { edit { typeText("1212121212") } }
            signIn { isEnabled() }
        }
    }

    @Test
    fun shouldValidateNationalIdentityNumbersForNorway() = run {
        activityRule.launch(SimpleSignAuthenticationActivity.newInstance(context(), Market.NO))

        SimpleSignAuthenticationScreen {
            signIn { isDisabled() }
            input { edit { typeText("12121212121") } }
            signIn { isEnabled() }
        }
    }
}

object SimpleSignAuthenticationScreen : KScreen<SimpleSignAuthenticationScreen>() {
    override val layoutId = R.layout.identity_input_fragment
    override val viewClass = SimpleSignAuthenticationActivity::class.java

    val signIn = KButton { withId(R.id.signIn) }
    val input = KTextInputLayout { withId(R.id.input) }
}
