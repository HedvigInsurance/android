package com.hedvig.android.feature.login.swedishlogin

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import com.eygraber.uri.Uri
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class BankIdStateJvmTest {
  @Test
  fun `the remembered BankID state never offers to open a BankID app`() = runComposeUiTest {
    var bankIdState: BankIdState? = null
    setContent {
      bankIdState = rememberBankIdState(Uri.parse("https://app.bankid.com/?autostarttoken=token&redirect=null"))
    }
    waitForIdle()

    assertThat(bankIdState).isNotNull()
    assertThat(bankIdState!!.canOpenBankId).isFalse()
  }
}
