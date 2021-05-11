package com.hedvig.app.testdata.feature.referrals

import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.fragment.ReferralFragment
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.testdata.common.builders.CostBuilder
import com.hedvig.app.testdata.feature.home.builders.HomeDataBuilder
import com.hedvig.app.testdata.feature.referrals.builders.EditCodeDataBuilder
import com.hedvig.app.testdata.feature.referrals.builders.LoggedInDataBuilder
import com.hedvig.app.testdata.feature.referrals.builders.ReferralsDataBuilder

val LOGGED_IN_DATA = LoggedInDataBuilder().build()
val LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED = LoggedInDataBuilder(
    features = listOf(Feature.REFERRALS, Feature.KEYGEAR)
).build()
val LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED = LoggedInDataBuilder(features = listOf(Feature.KEYGEAR)).build()
val LOGGED_IN_DATA_WITH_REFERRALS_ENABLED = LoggedInDataBuilder(features = listOf(Feature.REFERRALS)).build()

val REFERRALS_DATA_WITH_NO_DISCOUNTS = ReferralsDataBuilder().build()
val REFERRALS_DATA_WITH_ONE_REFEREE = ReferralsDataBuilder(
    insuranceCost = CostBuilder(
        discountAmount = "10.00",
        netAmount = "339.00"
    ).build(),
    costReducedIndefiniteDiscount = CostBuilder(
        discountAmount = "10.00",
        netAmount = "339.00"
    ).build(),
    referredBy = ReferralFragment(
        asActiveReferral = ReferralFragment.AsActiveReferral(
            name = "Example",
            discount = ReferralFragment.Discount(
                fragments = ReferralFragment.Discount.Fragments(
                    MonetaryAmountFragment(
                        amount = "10.00",
                        currency = "SEK"
                    )
                )
            )
        ),
        asTerminatedReferral = null,
        asInProgressReferral = null
    )
).build()

val REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT = ReferralsDataBuilder(
    insuranceCost = CostBuilder(
        discountAmount = "100.00",
        netAmount = "239.00"
    ).build(),
    costReducedIndefiniteDiscount = CostBuilder(
        discountAmount = "10.00",
        netAmount = "339.00"
    ).build(),
    referredBy = ReferralFragment(
        asActiveReferral = ReferralFragment.AsActiveReferral(
            name = "Example",
            discount = ReferralFragment.Discount(
                fragments = ReferralFragment.Discount.Fragments(
                    MonetaryAmountFragment(
                        amount = "10.00",
                        currency = "SEK"
                    )
                )
            )
        ),
        asTerminatedReferral = null,
        asInProgressReferral = null
    )
).build()

const val COMPLEX_REFERRAL_CODE = "CODE $ \uD83E\uDD11"

val REFERRALS_DATA_WITH_COMPLEX_CODE = ReferralsDataBuilder(
    code = COMPLEX_REFERRAL_CODE
).build()

val REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES = ReferralsDataBuilder(
    insuranceCost = CostBuilder(
        discountAmount = "10.00",
        netAmount = "339.00"
    ).build(),
    costReducedIndefiniteDiscount = CostBuilder(
        discountAmount = "10.00",
        netAmount = "339.00"
    ).build(),
    invitations = listOf(
        ReferralFragment(
            asActiveReferral = ReferralFragment.AsActiveReferral(
                name = "Example",
                discount = ReferralFragment.Discount(
                    fragments = ReferralFragment.Discount.Fragments(
                        MonetaryAmountFragment(
                            amount = "10.00",
                            currency = "SEK"
                        )
                    )
                )
            ),
            asInProgressReferral = null,
            asTerminatedReferral = null
        ),
        ReferralFragment(
            asActiveReferral = null,
            asInProgressReferral = ReferralFragment.AsInProgressReferral(
                name = "Example 2"
            ),
            asTerminatedReferral = null
        ),
        ReferralFragment(
            asActiveReferral = null,
            asInProgressReferral = null,
            asTerminatedReferral = ReferralFragment.AsTerminatedReferral(
                name = "Example 3"
            )
        ),
        ReferralFragment(
            __typename = "AcceptedReferral",
            asActiveReferral = null,
            asInProgressReferral = null,
            asTerminatedReferral = null
        )
    )
).build()

val EDIT_CODE_DATA_SUCCESS = EditCodeDataBuilder().build()
val EDIT_CODE_DATA_ALREADY_TAKEN = EditCodeDataBuilder(
    variant = EditCodeDataBuilder.ResultVariant.ALREADY_TAKEN
).build()

val EDIT_CODE_DATA_TOO_SHORT = EditCodeDataBuilder(
    variant = EditCodeDataBuilder.ResultVariant.TOO_SHORT
).build()

val EDIT_CODE_DATA_TOO_LONG = EditCodeDataBuilder(
    variant = EditCodeDataBuilder.ResultVariant.TOO_LONG
).build()

val EDIT_CODE_DATA_TOO_MANY_CHANGES = EditCodeDataBuilder(
    variant = EditCodeDataBuilder.ResultVariant.EXCEEDED_MAX_UPDATES
).build()

val EDIT_CODE_DATA_UNKNOWN_RESULT = EditCodeDataBuilder(
    variant = EditCodeDataBuilder.ResultVariant.UNKNOWN
).build()
