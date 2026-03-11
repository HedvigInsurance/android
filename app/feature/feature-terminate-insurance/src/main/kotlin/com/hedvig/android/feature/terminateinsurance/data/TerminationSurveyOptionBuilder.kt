package com.hedvig.android.feature.terminateinsurance.data

import kotlin.random.Random

/**
 * Builds the survey option tree for a termination flow.
 * Direct port of Odyssey's TerminationSurveyService.
 *
 * Each option has:
 * - id: matches the SurveyOption.Type enum name from Odyssey
 * - title: hardcoded display text (TODO: replace with Res.string resources later)
 * - subOptions: nested options for drill-down
 * - suggestion: optional action/info card
 * - feedBackRequired: whether free-text feedback input is shown
 *
 * Display strings are hardcoded English for now. When wiring up Lokalise/Res.string,
 * replace DISPLAY_NAMES values with stringResource() calls or a resolver.
 */
internal object TerminationSurveyOptionBuilder {

  fun build(
    typeOfContract: String,
    supportsBetterPrice: Boolean,
    supportsBetterCoverage: Boolean,
    memberId: String,
  ): List<TerminationSurveyOption> {
    val options = createOptions(typeOfContract, supportsBetterPrice, supportsBetterCoverage)
    return shuffleOptions(options, memberId)
  }

  private fun createOptions(
    typeOfContract: String,
    supportsBetterPrice: Boolean,
    supportsBetterCoverage: Boolean,
  ): List<TerminationSurveyOption> {
    val base = baseOptions()
      .mutateIf(
        supportsBetterPrice,
        option("BETTER_PRICE").withSubs(
          option("BETTER_PRICE_CHANGE_COVERAGE_LEVEL", suggestion = SurveyOptionSuggestion.Known.Action.DowngradePriceByChangingTier(
            description = "You can lower your price by changing your coverage level",
            buttonTitle = "Change coverage level",
            infoType = InfoType.INFO,
          )),
          option("BETTER_PRICE_CANCEL_WITHOUT_NEW_QUOTE", feedBackRequired = true),
          shuffle = false,
        ),
      )
      .mutateIf(
        supportsBetterCoverage,
        option("MISSING_COVERAGE_OR_TERMS").withSubs(
          option("MISSING_COVERAGE_OR_TERMS_CHANGE_COVERAGE_LEVEL", suggestion = SurveyOptionSuggestion.Known.Action.UpgradeCoverageByChangingTier(
            description = "You can upgrade your coverage level",
            buttonTitle = "Change coverage level",
            infoType = InfoType.INFO,
          )),
          option("MISSING_COVERAGE_OR_TERMS_CANCEL_WITHOUT_NEW_QUOTE", feedBackRequired = true),
          shuffle = false,
        ),
      )

    return when (typeOfContract) {
      "SE_HOUSE_BAS", "SE_HOUSE",
      "SE_APARTMENT_BRF_BAS", "SE_APARTMENT_BRF",
      "SE_APARTMENT_RENT_BAS", "SE_APARTMENT_RENT",
      "SE_QASA_SHORT_TERM_RENTAL", "SE_QASA_LONG_TERM_RENTAL",
      "SE_GROUP_APARTMENT_BRF", "SE_GROUP_APARTMENT_RENT",
      "SE_OBJECT_LEGACY",
      -> base
        .mutate(homeMovingOption(studentVariant = false))
        .mutateIf(
          supportsBetterCoverage,
          option("NO_LONGER_NEEDED").withSubs(
            option("MOVED_IN_WITH_SOMEONE"),
            option("MOVED_ABROAD_CHANGE_TIER", suggestion = SurveyOptionSuggestion.Known.Action.UpgradeCoverageByChangingTier(
              description = "You can change your coverage level before moving abroad",
              buttonTitle = "SURVEY_CHANGE_COVERAGE_LEVEL",
              infoType = InfoType.INFO,
            )),
            option("MOVED_OTHER", feedBackRequired = true),
            shuffle = false,
          ),
        )
        .mutateIf(
          !supportsBetterCoverage,
          option("NO_LONGER_NEEDED").withSubs(
            option("MOVED_IN_WITH_SOMEONE"),
            option("MOVED_ABROAD"),
            option("MOVED_OTHER", feedBackRequired = true),
          ),
        )

      "SE_HOUSE_MAX", "SE_APARTMENT_BRF_MAX", "SE_APARTMENT_RENT_MAX",
      -> base
        .mutate(
          homeMovingOption(studentVariant = false),
          option("NO_LONGER_NEEDED").withSubs(
            option("MOVED_IN_WITH_SOMEONE"),
            option("MOVED_ABROAD"),
            option("MOVED_OTHER", feedBackRequired = true),
          ),
        )

      "SE_APARTMENT_STUDENT_BRF", "SE_APARTMENT_STUDENT_RENT",
      -> base
        .mutate(homeMovingOption(studentVariant = true))
        .mutateIf(
          supportsBetterCoverage,
          option("NO_LONGER_NEEDED").withSubs(
            option("MOVED_IN_WITH_SOMEONE_STUDENT", suggestion = SurveyOptionSuggestion.Known.Info(
              description = "SURVEY_INFO_DESCRIPTION_STUDENT_MOVED_IN_WITH_SOMEONE",
              infoType = InfoType.INFO,
            )),
            option("MOVED_ABROAD_CHANGE_TIER", suggestion = SurveyOptionSuggestion.Known.Action.UpgradeCoverageByChangingTier(
              description = "You can change your coverage level before moving abroad",
              buttonTitle = "SURVEY_CHANGE_COVERAGE_LEVEL",
              infoType = InfoType.INFO,
            )),
            option("MOVED_OTHER", feedBackRequired = true),
            shuffle = false,
          ),
        )
        .mutateIf(
          !supportsBetterCoverage,
          option("NO_LONGER_NEEDED").withSubs(
            option("MOVED_IN_WITH_SOMEONE_STUDENT", suggestion = SurveyOptionSuggestion.Known.Info(
              description = "SURVEY_INFO_DESCRIPTION_STUDENT_MOVED_IN_WITH_SOMEONE",
              infoType = InfoType.INFO,
            )),
            option("MOVED_ABROAD"),
            option("MOVED_OTHER", feedBackRequired = true),
          ),
        )

      "SE_VACATION_HOME_BAS", "SE_VACATION_HOME_STANDARD",
      -> base.mutate(
        option("NO_LONGER_NEEDED").withSubs(
          option("SOLD_VACATION_HOME"),
          option("NO_LONGER_NEEDED_OTHER", feedBackRequired = true),
          shuffle = false,
        ),
      )

      "SE_CAR_TRAFFIC", "SE_CAR_HALF", "SE_CAR_FULL",
      "SE_CAR_TRIAL_HALF", "SE_CAR_TRIAL_FULL",
      -> base.mutate(
        option("NO_LONGER_NEEDED").withSubs(
          option("CAR_SOLD"),
          option("CAR_DECOMMISSIONED"),
          option("CAR_SCRAPPED"),
          option("CAR_OTHER", feedBackRequired = true),
          shuffle = false,
        ),
      )

      "SE_CAR_DECOMMISSIONED",
      -> base.mutate(
        option("NO_LONGER_NEEDED").withSubs(
          option("CAR_SOLD"),
          option("CAR_SCRAPPED"),
          option("CAR_RECOMMISSIONED"),
          option("CAR_OTHER", feedBackRequired = true),
          shuffle = false,
        ),
        option("MISSING_COVERAGE_OR_TERMS").withSubs(
          option("MISSING_COVERAGE_OR_TERMS_CHANGE_COVERAGE_LEVEL", suggestion = SurveyOptionSuggestion.Known.Action.UpgradeCoverageByChangingTier(
            description = "You can upgrade your coverage level",
            buttonTitle = "Change coverage level",
            infoType = InfoType.INFO,
          )),
          option("MISSING_COVERAGE_OR_TERMS_CANCEL_WITHOUT_NEW_QUOTE", feedBackRequired = true),
          shuffle = false,
        ),
      )

      "SE_CAT_BASIC", "SE_CAT_STANDARD", "SE_CAT_PREMIUM",
      "SE_DOG_BASIC", "SE_DOG_STANDARD", "SE_DOG_PREMIUM",
      -> base.mutate(
        option("NO_LONGER_NEEDED").withSubs(
          option("PET_NEW_OWNER"),
          option("PET_NO_LONGER_LIVES"),
          option("PET_OTHER", feedBackRequired = true),
          shuffle = false,
        ),
      )

      "SE_ACCIDENT", "SE_ACCIDENT_STUDENT",
      -> base.mutate(
        option("ACCIDENT_OTHER", feedBackRequired = true),
      )

      else -> base
    }
  }

  private fun baseOptions(): List<TerminationSurveyOption> = listOf(
    option("BETTER_PRICE", feedBackRequired = true),
    option("MISSING_COVERAGE_OR_TERMS", feedBackRequired = true),
    option("DISSATISFIED_SERVICE", feedBackRequired = true),
    option("OTHER_REASON", feedBackRequired = true),
  )

  private fun homeMovingOption(studentVariant: Boolean): TerminationSurveyOption {
    val movedInOption = if (studentVariant) {
      option("MOVED_IN_WITH_SOMEONE_STUDENT", suggestion = SurveyOptionSuggestion.Known.Info(
        description = "SURVEY_INFO_DESCRIPTION_STUDENT_MOVED_IN_WITH_SOMEONE",
        infoType = InfoType.INFO,
      ))
    } else {
      option("MOVED_IN_WITH_SOMEONE")
    }
    return option("MOVING").withSubs(
      option("MOVING_NEW_ADDRESS", suggestion = SurveyOptionSuggestion.Known.Action.UpdateAddress(
        description = "You can update your address instead of cancelling",
        buttonTitle = "Update address",
        infoType = InfoType.OFFER,
      )),
      movedInOption,
      option("MOVING_CANCEL_WITHOUT_NEW_QUOTE", feedBackRequired = true),
      shuffle = false,
    )
  }

  private fun option(
    id: String,
    feedBackRequired: Boolean = false,
    suggestion: SurveyOptionSuggestion? = null,
  ): TerminationSurveyOption = TerminationSurveyOption(
    id = id,
    listIndex = 0,
    title = DISPLAY_NAMES[id] ?: id,
    feedBackRequired = feedBackRequired,
    suggestion = suggestion,
    subOptions = emptyList(),
    shuffleSubOptions = true,
  )

  private fun TerminationSurveyOption.withSubs(
    vararg subs: TerminationSurveyOption,
    shuffle: Boolean = true,
  ): TerminationSurveyOption = copy(
    subOptions = subs.toList(),
    feedBackRequired = false,
    shuffleSubOptions = shuffle,
  )

  private fun List<TerminationSurveyOption>.mutate(
    vararg updates: TerminationSurveyOption,
  ): List<TerminationSurveyOption> {
    val result = this.toMutableList()
    updates.forEach { updated ->
      val index = result.indexOfFirst { it.id == updated.id }
      if (index >= 0) {
        result[index] = result[index].copy(
          subOptions = updated.subOptions,
          shuffleSubOptions = updated.shuffleSubOptions,
        )
      } else {
        result.add(updated)
      }
    }
    return result
  }

  private fun List<TerminationSurveyOption>.mutateIf(
    condition: Boolean,
    vararg updates: TerminationSurveyOption,
  ): List<TerminationSurveyOption> = if (condition) mutate(*updates) else this

  private fun shuffleOptions(
    options: List<TerminationSurveyOption>,
    memberId: String,
  ): List<TerminationSurveyOption> {
    val seed = memberId.hashCode().toLong()
    val random = Random(seed)
    val shuffled = options.shuffled(random)
    val (otherOptions, nonOtherOptions) = shuffled.partition {
      it.id.contains("other", ignoreCase = true)
    }
    return (nonOtherOptions + otherOptions).mapIndexed { index, opt ->
      opt.copy(
        listIndex = index,
        subOptions = shuffleSubOptions(opt.subOptions, memberId, opt.shuffleSubOptions),
      )
    }
  }

  private fun shuffleSubOptions(
    subs: List<TerminationSurveyOption>,
    memberId: String,
    shouldShuffle: Boolean,
  ): List<TerminationSurveyOption> {
    if (subs.isEmpty()) return subs
    if (!shouldShuffle) {
      return subs.mapIndexed { i, s -> s.copy(listIndex = i) }
    }
    val seed = memberId.hashCode().toLong()
    val random = Random(seed)
    val shuffled = subs.shuffled(random)
    val (others, nonOthers) = shuffled.partition { it.id.contains("other", ignoreCase = true) }
    return (nonOthers + others).mapIndexed { i, s -> s.copy(listIndex = i) }
  }

  private val DISPLAY_NAMES = mapOf(
    "MOVED_IN_WITH_SOMEONE" to "I'm moving in with someone",
    "MOVED_IN_WITH_SOMEONE_STUDENT" to "I'm moving in with someone",
    "MOVED_ABROAD" to "I'm moving abroad",
    "MOVED_ABROAD_CHANGE_TIER" to "I'm moving abroad",
    "MOVED_OTHER" to "Other",
    "MOVING" to "I'm moving",
    "MOVING_NEW_ADDRESS" to "I'm moving to a new address",
    "MOVING_CANCEL_WITHOUT_NEW_QUOTE" to "I want to cancel without a new quote",
    "CAR_SOLD" to "I've sold my car",
    "CAR_SCRAPPED" to "My car has been scrapped",
    "CAR_DECOMMISSIONED" to "My car is decommissioned",
    "CAR_RECOMMISSIONED" to "My car has been recommissioned",
    "CAR_OTHER" to "Other",
    "PET_NEW_OWNER" to "My pet has a new owner",
    "PET_NO_LONGER_LIVES" to "My pet no longer lives",
    "PET_OTHER" to "Other",
    "ACCIDENT_OTHER" to "I no longer need this insurance",
    "DISSATISFIED_COVERAGE" to "Missing coverage or terms",
    "DISSATISFIED_SERVICE" to "I'm dissatisfied with the service",
    "DISSATISFIED_APP" to "I'm dissatisfied with the app",
    "DISSATISFIED_PRICE" to "I'm dissatisfied with the price",
    "DISSATISFIED_OTHER" to "Other",
    "BETTER_OFFER" to "I no longer need this insurance",
    "BETTER_PRICE" to "I found a better price",
    "BETTER_PRICE_CHANGE_COVERAGE_LEVEL" to "Change coverage level",
    "BETTER_PRICE_CANCEL_WITHOUT_NEW_QUOTE" to "I want to cancel without a new quote",
    "MISSING_COVERAGE_OR_TERMS" to "Missing coverage or terms",
    "MISSING_COVERAGE_OR_TERMS_CHANGE_COVERAGE_LEVEL" to "Change coverage level",
    "MISSING_COVERAGE_OR_TERMS_CANCEL_WITHOUT_NEW_QUOTE" to "I want to cancel without a new quote",
    "DISSATISFIED" to "Other",
    "NO_LONGER_NEEDED" to "I no longer need this insurance",
    "OTHER_REASON" to "Other",
    "NO_LONGER_NEEDED_OTHER" to "Other",
    "SOLD_VACATION_HOME" to "I've sold my vacation home",
  )
}
