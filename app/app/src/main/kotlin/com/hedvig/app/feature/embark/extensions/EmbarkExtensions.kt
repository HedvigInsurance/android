package com.hedvig.app.feature.embark.extensions

import giraffe.EmbarkStoryQuery
import giraffe.fragment.ApiFragment

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
