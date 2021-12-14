package com.hedvig.app.feature.onboarding

import androidx.recyclerview.widget.DiffUtil

class OnboardingDiffUtilCallback : DiffUtil.ItemCallback<OnboardingModel>() {
    override fun areItemsTheSame(oldItem: OnboardingModel, newItem: OnboardingModel): Boolean {
        if (oldItem is OnboardingModel.BundleItem && newItem is OnboardingModel.BundleItem) {
            return when (oldItem.bundle.storyName) {
                newItem.bundle.storyName -> true
                else -> false
            }
        }
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: OnboardingModel, newItem: OnboardingModel) =
        oldItem == newItem
}
