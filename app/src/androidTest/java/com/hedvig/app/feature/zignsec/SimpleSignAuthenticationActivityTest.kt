package com.hedvig.app.feature.zignsec

import com.hedvig.app.R
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.hedvig.app.util.hasHelperText
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton
import org.junit.Rule
import org.junit.Test

class SimpleSignAuthenticationActivityTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(SimpleSignAuthenticationActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule()

    @Test
    fun shouldValidatePersonalIdentificationNumbersForDenmark() = run {
        activityRule.launch(SimpleSignAuthenticationActivity.newInstance(context(), Market.DK))

        SimpleSignAuthenticationScreen {
            signIn {
                isDisabled()
                hasText(R.string.simple_sign_sign_in_dk)
            }
            input {
                hasHint(R.string.simple_sign_login_text_field_label_dk)
                hasHelperText(R.string.simple_sign_login_text_field_helper_text_dk)
                edit { typeText("1212121212") }
            }
            signIn { isEnabled() }
        }
    }

    @Test
    fun shouldValidateNationalIdentityNumbersForNorway() = run {
        activityRule.launch(SimpleSignAuthenticationActivity.newInstance(context(), Market.NO))

        SimpleSignAuthenticationScreen {
            signIn {
                isDisabled()
                hasText(R.string.simple_sign_sign_in)
            }
            input {
                hasHint(R.string.simple_sign_login_text_field_label)
                hasHelperText(R.string.simple_sign_login_text_field_helper_text)
                edit { typeText("12121212121") }
            }
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
