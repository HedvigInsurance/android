package com.hedvig.android.feature.help.center.commonclaim

import hedvig.resources.R
import kotlinx.serialization.Serializable
import octopus.CommonClaimsQuery

sealed interface CommonClaim {
  val title: String
  val hintTextRes: Int?

  @Serializable
  data class Emergency(
    override val title: String,
    override val hintTextRes: Int?,
    val emergencyNumber: String,
  ) : CommonClaim {
    companion object {
      @Suppress("ktlint:standard:max-line-length")
      fun from(
        title: String,
        layout: CommonClaimsQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription.CommonClaimLayoutEmergencyLayout,
      ) = Emergency(
        title = title,
        emergencyNumber = layout.emergencyNumber,
        hintTextRes = R.string.HC_QUICK_ACTIONS_SICK_ABROAD_SUBTITLE, // as we only have one emergency: sick abroad
      )
    }
  }

  @Serializable
  data class Generic(
    val id: String,
    override val title: String,
    override val hintTextRes: Int?,
    val bulletPoints: List<BulletPoint>,
  ) : CommonClaim {
    @Serializable
    data class BulletPoint(
      val title: String,
      val description: String,
    )

    val isFirstVet = checkIfFirstVet(id)

    companion object {
      @Suppress("ktlint:standard:max-line-length")
      fun from(
        id: String,
        title: String,
        layout: CommonClaimsQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription.CommonClaimLayoutTitleAndBulletPointsLayout,
      ): Generic {
        val hintText = if (checkIfFirstVet(id)) R.string.HC_QUICK_ACTIONS_FIRSTVET_SUBTITLE else null
        return Generic(
          id = id,
          title = title,
          hintTextRes = hintText,
          bulletPoints = layout.bulletPoints.map { bulletPoint ->
            BulletPoint(
              bulletPoint.title,
              bulletPoint.description,
            )
          },
        )
      }
    }
  }
}

private fun checkIfFirstVet(id: String): Boolean {
  return id == "30" || id == "31" || id == "32"
}
