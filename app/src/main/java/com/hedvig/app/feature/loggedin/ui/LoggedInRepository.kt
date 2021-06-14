package com.hedvig.app.feature.loggedin.ui

import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.LoggedInQuery

interface LoggedInRepository {
    suspend fun loggedInData(): Response<LoggedInQuery.Data>
}
