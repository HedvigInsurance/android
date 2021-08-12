package com.hedvig.app.feature.embark

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

fun ApiFragment.AsEmbarkApiGraphQLQuery.getPassageNameFromError() = queryData
    .errors
    .first()
    .fragments
    .graphQLErrorsFragment
    .next
    .fragments
    .embarkLinkFragment
    .name

fun EmbarkStoryQuery.Action.api(index: Int): ApiFragment? =
    asEmbarkTextActionSet?.textSetData?.api?.fragments?.apiFragment
        ?: asEmbarkTextAction?.textData?.api?.fragments?.apiFragment
        ?: asEmbarkSelectAction?.selectData?.options?.getOrNull(index)?.api?.fragments?.apiFragment
