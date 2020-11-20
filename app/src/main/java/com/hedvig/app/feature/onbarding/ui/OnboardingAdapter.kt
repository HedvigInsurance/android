package com.hedvig.app.feature.onbarding.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PlanCalculatePriceButtonBinding
import com.hedvig.app.databinding.PlanCardBinding
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.onbarding.OnboardingDiffUtilCallback
import com.hedvig.app.feature.onbarding.OnboardingModel
import com.hedvig.app.feature.onbarding.OnboardingViewModel
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.doOnEnd
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.invalidData

class OnboardingAdapter(
    val viewModel: OnboardingViewModel,
    val marketProvider: MarketProvider
) :
    ListAdapter<OnboardingModel, OnboardingAdapter.ViewHolder>(OnboardingDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.plan_card -> ViewHolder.QuoteType(parent)
        R.layout.plan_info -> ViewHolder.Info(parent)
        R.layout.plan_calculate_price_button -> ViewHolder.Button(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is OnboardingModel.Quote -> R.layout.plan_card
        OnboardingModel.Info -> R.layout.plan_info
        OnboardingModel.Button -> R.layout.plan_calculate_price_button
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel, marketProvider)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            item: OnboardingModel,
            viewModel: OnboardingViewModel,
            marketProvider: MarketProvider,
        )

        class QuoteType(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.plan_card)) {
            private val binding by viewBinding(PlanCardBinding::bind)
            override fun bind(
                item: OnboardingModel,
                viewModel: OnboardingViewModel,
                marketProvider: MarketProvider,
            ) {
                if (item !is OnboardingModel.Quote) {
                    invalidData(item)
                    return
                }
                binding.apply {
                    radioButton.isChecked = false
                    container.setBackgroundResource(R.color.transparent)
                    discount.remove()
                    blur.remove()
                    when (item) {
                        is OnboardingModel.Quote.Bundle -> {
                            if (item.selected) {
                                container.setBackgroundResource(R.drawable.card_house_background)
                                blur.show()
                                blur.setColorFilter(blur.context.compatColor(R.color.color_card_blur_house))
                                radioButton.isChecked = true
                            }
                            discount.show()
                            description.text =
                                "TODO Combination of both contents and travel insurance"
                            name.text = "TODO Bundle"
                            root.setHapticClickListener {
                                animate(root.width.toFloat() + shimmer.width.toFloat())
                                viewModel.setSelectedQuoteType(OnboardingModel.Quote.Bundle(true))
                            }
                        }
                        is OnboardingModel.Quote.Content -> {
                            if (item.selected) {
                                container.setBackgroundResource(R.drawable.card_apartment_background)
                                blur.show()
                                blur.setColorFilter(blur.context.compatColor(R.color.color_card_blur_apartment))
                                radioButton.isChecked = true
                            }
                            description.text =
                                "TODO Contents insurance covers everything in your home"
                            name.text = "TODO Content"
                            root.setHapticClickListener {
                                animate(root.width.toFloat() + shimmer.width.toFloat())
                                viewModel.setSelectedQuoteType(OnboardingModel.Quote.Content(true))
                            }
                        }
                        is OnboardingModel.Quote.Travel -> {
                            if (item.selected) {
                                container.setBackgroundResource(R.drawable.card_travel_background)
                                blur.show()
                                blur.setColorFilter(blur.context.compatColor(R.color.color_card_blur_travel))
                                radioButton.isChecked = true
                            }
                            description.text =
                                "TODO Travel insurance protects you and your family when you're travelling"
                            name.text = "TODO Travel"
                            root.setHapticClickListener {
                                animate(root.width.toFloat() + shimmer.width.toFloat())
                                viewModel.setSelectedQuoteType(OnboardingModel.Quote.Travel(true))
                            }
                        }
                    }
                }
            }

            private fun animate(distance: Float) {
                binding.apply {
                    val shimmerStartPosition = shimmer.x
                    Handler(Looper.getMainLooper()).postDelayed({
                        ValueAnimator.ofFloat(shimmerStartPosition, distance).apply {
                            duration = 500

                            addUpdateListener { animation ->
                                shimmer.translationX = animation.animatedValue as Float
                            }
                            doOnEnd {
                                shimmer.translationX = shimmerStartPosition
                            }
                            start()
                        }
                    }, 300)
                }
            }
        }

        class Info(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.plan_info)) {
            override fun bind(
                item: OnboardingModel,
                viewModel: OnboardingViewModel,
                marketProvider: MarketProvider,
            ) {
            }
        }

        class Button(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.plan_calculate_price_button)) {
            private val binding by viewBinding(PlanCalculatePriceButtonBinding::bind)
            override fun bind(
                item: OnboardingModel,
                viewModel: OnboardingViewModel,
                marketProvider: MarketProvider,
            ) {
                binding.root.setHapticClickListener { button ->
                    button.context.startActivity(
                        WebOnboardingActivity.newNoInstance(
                            button.context,
                            viewModel.getSelectedNoPlan()
                        )
                    )
                }
            }
        }
    }
}
