package com.hedvig.android.feature.help.center.commonclaim

import kotlinx.serialization.Serializable
import octopus.CommonClaimsQuery

internal sealed interface CommonClaim {
  val title: String

  @Serializable
  data class Emergency(
    override val title: String,
    val emergencyNumber: String,
  ) : CommonClaim {
    companion object {
      fun from(
        title: String,
        layout: CommonClaimsQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription.CommonClaimLayoutEmergencyLayout,
      ) = Emergency(
        title = title,
        emergencyNumber = layout.emergencyNumber,
      )
    }
  }

  @Serializable
  data class Generic(
    val id: String,
    override val title: String,
    val bulletPoints: List<BulletPoint>,
  ) : CommonClaim {
    @Serializable
    data class BulletPoint(
      val title: String,
      val description: String,
    )

    val isFirstVet = id == "30" || id == "31" || id == "32"

    companion object {
      fun from(
        id: String,
        title: String,
        layout: CommonClaimsQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription.CommonClaimLayoutTitleAndBulletPointsLayout,
      ) = Generic(
        id = id,
        title = title,
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
