package com.hedvig.android.odyssey.search

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.CommonClaimsQuery
import com.hedvig.android.apollo.graphql.type.CommonClaim
import com.hedvig.android.apollo.graphql.type.Locale
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.language.LanguageService
import com.hedvig.android.odyssey.model.ItemProblem
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim

class GetCommonClaimsUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) : GetClaimEntryPoints {

  override suspend operator fun invoke(): CommonClaimsResult {
    return apolloClient
      .query(commonClaimsQuery())
      .safeExecute()
      .let { result ->
        when (result) {
          is OperationResult.Error -> CommonClaimsResult.Error(result.message ?: "Unknown error")
          is OperationResult.Success -> {
            val searchableClaims = createSearchableClaims(result)
            CommonClaimsResult.Success(searchableClaims)
          }
        }
      }
  }

  private fun createSearchableClaims(result: OperationResult.Success<CommonClaimsQuery.Data>): List<SearchableClaim> {
    return result.data.commonClaims.map { commonClaim ->
      SearchableClaim(
        id = commonClaim.id,
        displayName = commonClaim.title,
        itemType = commonClaim.itemTypeFromId(languageService.getLanguage().toLocale()),
        itemProblem = ItemProblem("BROKEN"),
        icon = SearchableClaim.Icon(
          darkUrl = commonClaim.icon.variants.fragments.iconVariantsFragment.dark.svgUrl,
          lightUrl = commonClaim.icon.variants.fragments.iconVariantsFragment.light.svgUrl,
        ),
      )
    }
  }

  private fun CommonClaimsQuery.CommonClaim.itemTypeFromId(locale: Locale): ItemType? = when (locale) {
    Locale.sv_SE -> when (id) {
      "5" -> ItemType("PHONE")
      else -> null
    }
    Locale.en_SE -> when (id) {
      "6" -> ItemType("PHONE")
      else -> null
    }
    Locale.nb_NO -> when (id) {
      "14" -> ItemType("PHONE")
      else -> null
    }
    Locale.en_NO -> when (id) {
      "15" -> ItemType("PHONE")
      else -> null
    }
    Locale.da_DK -> when (id) {
      "21" -> ItemType("PHONE")
      else -> null
    }
    Locale.en_DK -> when (id) {
      "22" -> ItemType("PHONE")
      else -> null
    }
    else -> null
  }

  private fun commonClaimsQuery() = CommonClaimsQuery(languageService.getGraphQLLocale())
}

sealed interface CommonClaimsResult {
  data class Error(val message: String) : CommonClaimsResult
  data class Success(val searchableClaims: List<SearchableClaim>) : CommonClaimsResult
}
