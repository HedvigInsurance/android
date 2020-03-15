package com.hedvig.app.data

import android.content.Context
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.ImportantMessagesQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import io.reactivex.Observable

class ImportantMessagesRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun fetchImportantMessages(): Observable<Response<ImportantMessagesQuery.Data>> {
        val dashboardQuery = ImportantMessagesQuery
            .builder()
            .languageCode(defaultLocale(context).rawValue())
            .build()

        return Rx2Apollo.from(apolloClientWrapper.apolloClient.query(dashboardQuery))
    }
}
