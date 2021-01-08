package com.hedvig.app.testdata.feature.profile

import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.app.testdata.feature.profile.builders.ProfileDataBuilder

val PROFILE_DATA = ProfileDataBuilder().build()

val PROFILE_DATA_BANK_ACCOUNT_ACTIVE = ProfileDataBuilder(directDebitStatus = DirectDebitStatus.ACTIVE).build()
