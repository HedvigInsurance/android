package com.hedvig.app.testdata.feature.insurance.builders

import com.hedvig.app.util.toArrayList
import giraffe.InsuranceQuery
import giraffe.fragment.PerilFragmentV2
import giraffe.type.PerilV2

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

    private val PERIL_FRAGMENT = PerilFragmentV2(
      title = "Mock",
      description = "Mock",
      icon = PerilFragmentV2.Icon(
        variants = PerilFragmentV2.Variants(
          dark = PerilFragmentV2.Dark(
            svgUrl = "/app-content-service/fire_dark.svg",
          ),
          light = PerilFragmentV2.Light(
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
