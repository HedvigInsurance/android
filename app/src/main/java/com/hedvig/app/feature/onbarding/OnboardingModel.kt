package com.hedvig.app.feature.onbarding

import androidx.recyclerview.widget.DiffUtil

sealed class OnboardingModel {
    sealed class Quote : OnboardingModel() {
        data class Bundle(val selected: Boolean) : Quote()
        data class Content(val selected: Boolean) : Quote()
        data class Travel(val selected: Boolean) : Quote()
    }

    object Info : OnboardingModel()
    object Button : OnboardingModel()
}

class OnboardingDiffUtilCallback : DiffUtil.ItemCallback<OnboardingModel>() {
    override fun areItemsTheSame(oldItem: OnboardingModel, newItem: OnboardingModel): Boolean {
        if (oldItem is OnboardingModel.Quote && newItem is OnboardingModel.Quote) {
            return when {
                oldItem is OnboardingModel.Quote.Bundle && newItem is OnboardingModel.Quote.Bundle -> true
                oldItem is OnboardingModel.Quote.Content && newItem is OnboardingModel.Quote.Content -> true
                oldItem is OnboardingModel.Quote.Travel && newItem is OnboardingModel.Quote.Travel -> true
                else -> false
            }
        }

        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: OnboardingModel, newItem: OnboardingModel) =
        oldItem == newItem
}
