package com.hedvig.app.di

import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.feature.loggedin.ui.LoggedInRepository
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import javax.inject.Inject

class LoggedInRepositoryMock @Inject constructor() : LoggedInRepository {
    override suspend fun loggedInData(): Response<LoggedInQuery.Data> {
        return Response.builder<LoggedInQuery.Data>(
            LoggedInQuery(Locale.EN_SE)
        )
            .data(LOGGED_IN_DATA_WITH_REFERRALS_ENABLED)
            .build()
    }
}
