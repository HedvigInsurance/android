package com.hedvig.android.feature.onboarding.navigation

import com.hedvig.android.core.common.KeepKmp
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.SuppressesAppStoreReviewRequest
import kotlinx.serialization.Serializable

/**
 * The onboarding flow root, rendering the welcome step. Pushed on top of Home by the onboarding
 * gate in `:app` for members who have not seen onboarding yet.
 *
 * It stays on the back stack for the whole flow, including while a step hosts a shared flow such as
 * edit co-insured, which is what makes it the right place to hang [SuppressesAppStoreReviewRequest].
 */
@KeepKmp
@Serializable
data object OnboardingKey : HedvigNavKey, SuppressesAppStoreReviewRequest
