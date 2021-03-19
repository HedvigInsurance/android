package com.hedvig.app.feature.embark

import com.hedvig.android.owldroid.fragment.ApiFragment

fun ApiFragment.AsEmbarkApiGraphQLQuery.getPassageNameFromError() = queryData
    .errors
    .first()
    .fragments
    .graphQLErrorsFragment
    .next
    .fragments
    .embarkLinkFragment
    .name
