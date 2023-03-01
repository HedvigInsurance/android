package com.hedvig.android.odyssey.repository

import android.util.Log
import com.hedvig.odyssey.remote.money.MonetaryAmount
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class AutomationClaimDTO2(
  val id: String,
  val status: AutomationClaimStatus,
  val dateOfOccurrence: String?,
  val location: ClaimLocation?,
  val audioUrl: String?,
  val items: List<AutomationClaimItemDTO2>,
  val inputs: List<AutomationClaimInputDTO2>,
  val resolutions: List<AutomationClaimResolutionDTO2>,
) {

  enum class ClaimLocation {
    AT_HOME,
    IN_HOME_COUNTRY,
    IN_HOME_MUNICIPALITY,
    OUTSIDE_HOME_MUNICIPALITY,
    ABROAD,
    ;

    fun getText() = when (this) {
      AT_HOME -> "At home"
      IN_HOME_COUNTRY -> "In home country"
      IN_HOME_MUNICIPALITY -> "In home municipality"
      OUTSIDE_HOME_MUNICIPALITY -> "Outside home municipality"
      ABROAD -> "Abroad"
    }
  }

  enum class AutomationClaimStatus {
    CREATING,
    OPENED,
  }

  init {
    if (inputs.any { it is AutomationClaimInputDTO2.Unknown }) {
      Log.d("ClaimDTO", "Unknown input retrieved from backend for claim with id=$id")
    }
  }
}

@kotlinx.serialization.Serializable
data class AutomationClaimItemDTO2(
  val typeId: String,
  val brandId: String?,
  val brandName: String?,
  val modelId: String?,
  val modelName: String?,
  val modelImageUrl: String?,
  val customName: String?,
  val problemIds: List<String>,
  val purchaseDate: String?,
  val purchasePrice: MonetaryAmount?,
)

@kotlinx.serialization.Serializable
sealed class AutomationClaimInputDTO2 {

  @kotlinx.serialization.Serializable
  @SerialName("AudioRecording")
  data class AudioRecording(
    val audioUrl: String?,
    val questions: List<AudioRecordingQuestion>,
  ) : AutomationClaimInputDTO2() {
    enum class AudioRecordingQuestion {
      CLAIM_QUESTION_WHAT_HAS_HAPPENED,
      CLAIM_QUESTION_WHERE_AND_WHEN_DID_IT_HAPPEN,
      CLAIM_QUESTION_WHERE_DID_IT_HAPPEN,
      CLAIM_QUESTION_WHAT_WHO_TOOK_DAMAGE_OR_NEEDS_REPLACEMENT,
      ;

      fun getText(): String = when (this) {
        CLAIM_QUESTION_WHAT_HAS_HAPPENED -> "What has happened?"
        CLAIM_QUESTION_WHERE_AND_WHEN_DID_IT_HAPPEN -> "Where and when did it happen?"
        CLAIM_QUESTION_WHAT_WHO_TOOK_DAMAGE_OR_NEEDS_REPLACEMENT -> "What needs replacement?"
        CLAIM_QUESTION_WHERE_DID_IT_HAPPEN -> "Where did it happen?"
      }
    }
  }

  @kotlinx.serialization.Serializable
  @SerialName("DateOfOccurrence")
  data class DateOfOccurrence(
    val dateOfOccurrence: String?,
  ) : AutomationClaimInputDTO2()

  @kotlinx.serialization.Serializable
  @SerialName("Location")
  data class Location(
    val location: AutomationClaimDTO2.ClaimLocation?,
    val options: List<AutomationClaimDTO2.ClaimLocation>,
  ) : AutomationClaimInputDTO2()

  @kotlinx.serialization.Serializable
  @SerialName("SingleItem")
  data class SingleItem(
    val typeId: String,
    val brandId: String?,
    val modelId: String?,
    val problemIds: List<ClaimProblem>,
    val purchaseDate: String?,
    val purchasePrice: MonetaryAmount?,
    val preferredCurrency: String,
    val options: ItemOptions,
  ) : AutomationClaimInputDTO2() {
    @kotlinx.serialization.Serializable
    class ItemOptions(
      val itemBrands: List<ItemBrandOption>,
      val itemModels: List<ItemModelOption>,
      val itemProblems: List<ItemProblemOption>,
    ) {
      @kotlinx.serialization.Serializable
      class ItemBrandOption(
        val typeId: String,
        val brandId: String,
      )

      @kotlinx.serialization.Serializable
      class ItemModelOption(
        val modelId: String,
        val modelName: String,
        val modelImageUrl: String?,
        val typeId: String,
        val brandId: String,
      )

      @kotlinx.serialization.Serializable
      class ItemProblemOption(
        val problemId: ClaimProblem,
      )
    }

    enum class ClaimProblem {
      BROKEN,
      BROKEN_FRONT,
      BROKEN_OTHER,
      BROKEN_BACK,
      WATER_DAMAGED,
      MISSING,
      STOLEN,
      ROBBED,
      FORGOTTEN,
      OTHER,
      BROKEN_SCREEN,
      
      ;

      fun getText() = when (this) {
        BROKEN -> "Broken"
        BROKEN_FRONT -> "Front"
        BROKEN_BACK -> "Back"
        BROKEN_OTHER -> "Other"
        WATER_DAMAGED -> "Water damage"
        MISSING -> "Missing"
        STOLEN -> "Stolen"
        ROBBED -> "Robbed"
        FORGOTTEN -> "Forgotten"
        OTHER -> "Other"
        BROKEN_SCREEN -> "Broken Screen"
      }
    }
  }

  @kotlinx.serialization.Serializable
  @SerialName("Unknown")
  object Unknown : AutomationClaimInputDTO2()
}

@kotlinx.serialization.Serializable
sealed class AutomationClaimResolutionDTO2 {
  @kotlinx.serialization.Serializable
  class SingleItemPayout(
    val purchasePrice: MonetaryAmount,
    val depreciation: MonetaryAmount,
    val deductible: MonetaryAmount,
    val payoutAmount: MonetaryAmount,
    val methods: List<AutomationClaimResolutionPayoutMethodDTO2>,
  ) : AutomationClaimResolutionDTO2()
}

@kotlinx.serialization.Serializable
sealed class AutomationClaimResolutionPayoutMethodDTO2 {
  @kotlinx.serialization.Serializable
  object AutomaticAutogiroPayout : AutomationClaimResolutionPayoutMethodDTO2()
}
