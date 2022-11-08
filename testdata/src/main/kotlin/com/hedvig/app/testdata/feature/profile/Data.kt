package com.hedvig.app.testdata.feature.profile

import com.hedvig.android.apollo.graphql.type.DirectDebitStatus
import com.hedvig.app.testdata.feature.profile.builders.ProfileDataBuilder

val PROFILE_DATA = ProfileDataBuilder().build()

val PROFILE_DATA_BANK_ACCOUNT_ACTIVE = ProfileDataBuilder(directDebitStatus = DirectDebitStatus.ACTIVE).build()

val PROFILE_DATA_ADYEN_CONNECTED = ProfileDataBuilder(
  directDebitStatus = DirectDebitStatus.ACTIVE,
  adyenConnected = true,
).build()
val PROFILE_DATA_ADYEN_NOT_CONNECTED = ProfileDataBuilder(adyenConnected = false).build()
