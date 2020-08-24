package com.hedvig.app.testdata.feature.payment

import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus

val PAYIN_STATUS_DATA_NEEDS_SETUP = PayinStatusQuery.Data(PayinMethodStatus.NEEDS_SETUP)
val PAYIN_STATUS_DATA_ACTIVE = PayinStatusQuery.Data(PayinMethodStatus.ACTIVE)
