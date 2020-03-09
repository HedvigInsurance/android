package com.hedvig.app.feature.marketpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.feature.language.LanguageAndMarketViewModel
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.market_item.view.*

class MarketAdapter(private val model: LanguageAndMarketViewModel) :
    RecyclerView.Adapter<MarketAdapter.ViewHolder>() {
    var items: List<MarketModel> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position].selected) {
            animateRadioButton(holder)
            holder.button.background =
                holder.button.context.getDrawable(R.drawable.ic_radio_button_checked)
        } else {
            holder.button.background =
                holder.button.context.getDrawable(R.drawable.ic_radio_button_unchecked)
        }
        when (position) {
            Market.SE.ordinal -> holder.itemView.apply {
                flag.setImageDrawable(context.compatDrawable(R.drawable.ic_flag_se))
                country.text = holder.itemView.context.getText(R.string.sweden)
            }
            Market.NO.ordinal -> holder.itemView.apply {
                flag.setImageDrawable(context.compatDrawable(R.drawable.ic_flag_no))
                country.text = holder.itemView.context.getText(R.string.norway)
            }
        }

        holder.itemView.setHapticClickListener {
            selectMarket(position)
        }
    }

    private fun selectMarket(position: Int) {
        when (position) {
            Market.SE.ordinal -> {
                model.updateMarket(Market.SE)
            }
            Market.NO.ordinal -> {
                model.updateMarket(Market.NO)
            }
        }
    }

    private fun animateRadioButton(holder: ViewHolder) {
        holder.button.apply {
            scaleX = 0f
            scaleY = 0f
            spring(
                SpringAnimation.SCALE_X,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(1f)
            spring(
                SpringAnimation.SCALE_Y,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(1f)
        }
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.market_item,
            parent,
            false
        )
    ) {
        val button: RadioButton = itemView.radioButton
    }

    companion object {
        private const val SV = 0
        private const val NO = 1
    }
}
