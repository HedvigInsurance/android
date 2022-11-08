package com.hedvig.app.testdata.feature.insurance.builders

import com.hedvig.android.apollo.graphql.InsuranceQuery
import com.hedvig.android.apollo.graphql.fragment.PerilFragment
import com.hedvig.android.apollo.graphql.type.PerilV2
import com.hedvig.app.util.toArrayList

class PerilBuilder {

  fun insuranceQueryBuild(noOfPerils: Int) = insuranceQueryPerils(noOfPerils)

  companion object {
    private fun insuranceQueryPerils(noOfPerils: Int): List<InsuranceQuery.ContractPeril> {
      val perilList: List<InsuranceQuery.ContractPeril> = buildList {
        for (i in 0..noOfPerils) {
          add(
            InsuranceQuery.ContractPeril(
              __typename = PerilV2.type.name,
              fragments = InsuranceQuery.ContractPeril.Fragments(PERIL_FRAGMENT),
            ),
          )
        }
      }
      return perilList
    }

    private val PERIL_FRAGMENT = PerilFragment(
      title = "Mock",
      description = "Mock",
      icon = PerilFragment.Icon(
        variants = PerilFragment.Variants(
          dark = PerilFragment.Dark(
            svgUrl = "/app-content-service/fire_dark.svg",
          ),
          light = PerilFragment.Light(
            svgUrl = "/app-content-service/fire.svg",
          ),
        ),
      ),
      covered = listOf(
        "Covered",
        "Covered",
        "Covered",
        "Covered",
        "Covered",
        "Covered",
      ).toArrayList(),
      exceptions = listOf(
        "Exceptions",
        "Exceptions",
        "Exceptions",
        "Exceptions",
        "Exceptions",
      ).toArrayList(),
      info = "Du kan få ersättning om tvättmaskinen säckar ihop eller om annan elektrisk " +
        "maskin eller apparat går sönder p.g.a. kortslutning, överslag eller överspänning.",

    )
  }
}
