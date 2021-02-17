package com.hedvig.onboarding.chooseplan

import androidx.recyclerview.widget.DiffUtil

class OnboardingDiffUtilCallback : DiffUtil.ItemCallback<OnboardingModel>() {
    override fun areItemsTheSame(oldItem: OnboardingModel, newItem: OnboardingModel): Boolean {
        if (oldItem is OnboardingModel.Bundle && newItem is OnboardingModel.Bundle) {
            return when (oldItem.embarkStory.name) {
                newItem.embarkStory.name -> true
                else -> false
            }
        }
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: OnboardingModel, newItem: OnboardingModel) =
        oldItem == newItem
}
