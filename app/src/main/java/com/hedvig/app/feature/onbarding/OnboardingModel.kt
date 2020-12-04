package com.hedvig.app.feature.onbarding

import androidx.recyclerview.widget.DiffUtil
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery

sealed class OnboardingModel {

    data class Bundle(
        val selected: Boolean,
        val embarkStory: ChoosePlanQuery.EmbarkStory
    ) : OnboardingModel()

    object Error : OnboardingModel()
}

class OnboardingDiffUtilCallback : DiffUtil.ItemCallback<OnboardingModel>() {
    override fun areItemsTheSame(oldItem: OnboardingModel, newItem: OnboardingModel): Boolean {
        if (oldItem is OnboardingModel.Bundle && newItem is OnboardingModel.Bundle) {
            return true
        }
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: OnboardingModel, newItem: OnboardingModel) =
        oldItem == newItem
}
