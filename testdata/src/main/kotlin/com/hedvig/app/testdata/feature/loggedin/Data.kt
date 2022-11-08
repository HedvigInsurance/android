package com.hedvig.app.testdata.feature.loggedin

import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.feature.loggedin.builders.ContractStatusDataBuilder
import com.hedvig.app.testdata.feature.loggedin.builders.WelcomeDataBuilder
import com.hedvig.app.testdata.feature.loggedin.builders.WhatsNewDataBuilder

val CONTRACT_STATUS_DATA_ONE_TERMINATED_CONTRACT = ContractStatusDataBuilder(
  listOf(ContractStatus.TERMINATED),
).build()

val WELCOME_DATA_ONE_PAGE = WelcomeDataBuilder().build()

val WHATS_NEW = WhatsNewDataBuilder().build()
val NO_WHATS_NEW = WhatsNewDataBuilder(listOf()).build()
