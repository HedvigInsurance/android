package com.hedvig.app.testdata.dashboard

import com.hedvig.android.apollo.graphql.InsuranceQuery
import com.hedvig.android.apollo.graphql.fragment.CrossSellFragment
import com.hedvig.android.apollo.graphql.type.CrossSell
import com.hedvig.android.apollo.graphql.type.CrossSellEmbark
import com.hedvig.android.apollo.graphql.type.CrossSellType
import com.hedvig.android.apollo.graphql.type.TypeOfContract
import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.dashboard.builders.InsuranceDataBuilder

val INSURANCE_DATA =
  InsuranceDataBuilder(
    contracts = listOf(ContractStatus.ACTIVE),
  ).build()

val INSURANCE_DATA_STUDENT =
  InsuranceDataBuilder(
    contracts = listOf(ContractStatus.ACTIVE),
    displayName = "Hemförsäkring Student",
  ).build()

val INSURANCE_DATA_ACTIVE_AND_TERMINATED =
  InsuranceDataBuilder(
    contracts = listOf(ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE),
  ).build()
val INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.ACTIVE, ContractStatus.TERMINATED),
).build()
val INSURANCE_DATA_DANISH_HOME_CONTENTS = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.ACTIVE),
).build()
val INSURANCE_DATA_DANISH_ACCIDENT = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.ACTIVE),
).build()
val INSURANCE_DATA_TERMINATED = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.TERMINATED),
).build()

val INSURANCE_DATA_WITH_CROSS_SELL = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.ACTIVE),
  crossSells = listOf(
    InsuranceQuery.PotentialCrossSell(
      __typename = CrossSell.type.name,
      fragments = InsuranceQuery.PotentialCrossSell.Fragments(
        crossSellFragment = CrossSellFragment(
          title = "Accident Insurance",
          description = "179 SEK/mo.",
          callToAction = "Calculate price",
          type = CrossSellType.ACCIDENT,
          contractType = TypeOfContract.SE_ACCIDENT,
          action = CrossSellFragment.Action(
            __typename = CrossSellEmbark.type.name,
            asCrossSellEmbark = CrossSellFragment.AsCrossSellEmbark(
              __typename = CrossSellEmbark.type.name,
              embarkStoryV2 = CrossSellFragment.EmbarkStoryV2(
                name = "123",
              ),
            ),
          ),
          blurHash = "LJC6\$2-:DiWB~WxuRkayMwNGo~of",
          imageUrl = "https://images.unsplash.com/photo-1628996796855-0b056a464e06",
          info = CrossSellFragment.Info(
            displayName = "Accident Insurance",
            aboutSection = "If you or a family member is injured in an accident insurance, " +
              "Hedvig is able to" +
              " compensate you for a hospital stay, rehabilitation, therapy and dental injuries. \n" +
              "\n" +
              "In case of a permanent injury that affect your your quality of life and ability " +
              "to work, an accident insurance can complement the support from " +
              "the social welfare system and your employer.",
            contractPerils = emptyList(),
            insuranceTerms = emptyList(),
            highlights = emptyList(),
            faq = emptyList(),
            insurableLimits = emptyList(),
          ),
        ),
      ),
    ),
  ),
).build()

val INSURANCE_DATA_UPCOMING_AGREEMENT = InsuranceDataBuilder(
  contracts = listOf(ContractStatus.ACTIVE),
  showUpcomingAgreement = true,
).build()
